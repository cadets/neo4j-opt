/*
 *   Copyright (c) 2018.
 *   This file is part of NeGraph.
 *
 *  NeGraph is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NeGraph is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NeGraph.  If not, see <https://www.gnu.org/licenses/>.
 * @author Jyothish Soman, cl cam uk
 */

package org.cam.storage.levelgraph.storage.dataqueue;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Math.min;

public class StackFile implements Closeable, Iterable<byte[]> {

    /**
     * Initial file size in bytes.
     */
    private static final int INITIAL_LENGTH = 4096; // one file system block

    /**
     * A block of nothing to write over old data.
     */
    private static final byte[] ZEROES = new byte[INITIAL_LENGTH];

    /**
     * The underlying file. Uses a ring buffer to store entries. Designed so that a modification
     * isn't committed or visible until we write the header. The header is much smaller than a
     * segment. So long as the underlying file system supports atomic segment writes, changes to the
     * queue are atomic. Storing the file length ensures we can recover from a failed expansion
     * (i.e. if setting the file length succeeds but the process dies before the data can be copied).
     * <p>
     * This implementation supports the following on-disk format.
     * <pre>
     *
     * Legacy Header (16 bytes):
     *   1 bit            Legacy indicator, always 0 (see "Header")
     *   31 bits          File length
     *   4 bytes          Element count
     *   4 bytes          Head element position
     *   4 bytes          Tail element position
     *
     * Element:
     *   4 bytes          Data length
     *   ...              Data
     * </pre>
     */
    private final RandomAccessFile raf;

    /**
     * Keep file around for error reporting.
     */
    private final File stackFile;


    /**
     * The header length in bytes: 16 or 32.
     */
    private final int stackHeaderLength;
    /**
     * In-memory reusableHeaderBuffer. Big enough to hold the header.
     */
    private final byte[] reusableHeaderBuffer = new byte[32];
    /**
     * When true, removing an element will also overwrite data with zero bytes.
     */
    private final boolean zero;
    /**
     * Cached file length. Always a power of 2.
     */
    private long stackFileLength;
    /**
     * Number of elements.
     */
    private int elementCount;
    /**
     * Pointer to top (or eldest) element.
     */
    private Element top;
    /**
     * Pointer to bottom (or newest) element.
     */
    private Element bottom;
    /**
     * The number of times this file has been structurally modified — it is incremented during
     * {@link #remove(int)} and {@link #add(byte[], int, int)}. Used by {@link ElementIterator}
     * to guard against concurrent modification.
     */
    private int modCount = 0;
    private boolean closed;

    private StackFile(File file, RandomAccessFile raf, boolean zero, boolean forceLegacy) throws IOException {
        this.stackFile = file;
        this.raf = raf;
        this.zero = zero;

        raf.seek(0);
        raf.readFully(reusableHeaderBuffer);

        long firstOffset;
        long lastOffset;

        stackHeaderLength = 16;

        stackFileLength = readInt(reusableHeaderBuffer, 0);
        elementCount = readInt(reusableHeaderBuffer, 4);
        firstOffset = readInt(reusableHeaderBuffer, 8);
        lastOffset = readInt(reusableHeaderBuffer, 12);


        if (stackFileLength > raf.length()) {
            throw new IOException(
                    "File is truncated. Expected length: " + stackFileLength + ", Actual length: " + raf.length());
        } else if (stackFileLength <= stackHeaderLength) {
            throw new IOException(
                    "File is corrupt; length stored in header (" + stackFileLength + ") is invalid.");
        }

        top = readElement(firstOffset);
        bottom = readElement(lastOffset);
    }

    private static RandomAccessFile initializeFromFile(File file, boolean forceLegacy)
            throws IOException {
        if (!file.exists() || (file.length() == 0)) {
            // Use a temp file so we don't leave a partially-initialized file.
            File tempFile = new File(file.getPath() + ".tmp");
            try (RandomAccessFile raf = open(tempFile)) {
                raf.setLength(INITIAL_LENGTH);
                raf.seek(0);
                raf.writeInt(INITIAL_LENGTH);
            }

            // A rename is atomic.
            if (!tempFile.renameTo(file)) {
                throw new IOException("Rename failed!");
            }
        }

        return open(file);
    }

    /**
     * Opens a random access file that writes synchronously.
     */
    private static RandomAccessFile open(File file) throws FileNotFoundException {
        return new RandomAccessFile(file, "rwd");
    }

    /**
     * Stores an {@code int} in the {@code byte[]}. The behavior is equivalent to calling
     * {@link RandomAccessFile#writeInt}.
     */
    private static void writeInt(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) (value >> 24);
        buffer[offset + 1] = (byte) (value >> 16);
        buffer[offset + 2] = (byte) (value >> 8);
        buffer[offset + 3] = (byte) value;
    }

    /**
     * Reads an {@code int} from the {@code byte[]}.
     */
    private static int readInt(byte[] buffer, int offset) {
        return ((buffer[offset] & 0xff) << 24)
                + ((buffer[offset + 1] & 0xff) << 16)
                + ((buffer[offset + 2] & 0xff) << 8)
                + (buffer[offset + 3] & 0xff);
    }


    /**
     * Use this to throw checked exceptions from iterator methods that do not declare that they throw
     * checked exceptions.
     */
    @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
    static <T extends Throwable> T getSneakyThrowable(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Writes header atomically. The arguments contain the updated values. The class member fields
     * should not have changed yet. This only updates the state in the file. It's up to the caller to
     * update the class member variables *after* this call succeeds. Assumes segment writes are
     * atomic in the underlying file system.
     */
    private void writeHeader(long fileLength, int elementCount, long firstPosition, long lastPosition)
            throws IOException {
        raf.seek(0);

        // Legacy queue header.
        writeInt(reusableHeaderBuffer, 0, (int) fileLength); // Signed, so leading bit is always 0 aka legacy.
        writeInt(reusableHeaderBuffer, 4, elementCount);
        writeInt(reusableHeaderBuffer, 8, (int) firstPosition);
        writeInt(reusableHeaderBuffer, 12, (int) lastPosition);
        raf.write(reusableHeaderBuffer, 0, 16);
    }

    private Element readElement(long position) throws IOException {
        if (position == 0) return Element.NULL;
        ringRead(position - Element.HEADER_LENGTH, reusableHeaderBuffer, Element.HEADER_LENGTH);
        int length = readInt(reusableHeaderBuffer, 0);
        return new Element(position, length);
    }


    /**
     * Writes count bytes from reusableHeaderBuffer to position in file. Automatically wraps write if position is
     * past the end of the file or if reusableHeaderBuffer overlaps it.
     *
     * @param position in file to write to
     * @param buffer   to write from
     * @param count    # of bytes to write
     */
    private void ringWrite(long position, byte[] buffer, int offset, int count) throws IOException {
        if (position + count <= stackFileLength) {
            raf.seek(position);
            raf.write(buffer, offset, count);
        } else {
            // The write overlaps the EOF.
            // # of bytes to write before the EOF. Guaranteed to be less than Integer.MAX_VALUE.
            int beforeEof = (int) (stackFileLength - position);
            raf.seek(position);
            raf.write(buffer, offset, beforeEof);
            raf.seek(stackHeaderLength);
            raf.write(buffer, offset + beforeEof, count - beforeEof);
        }
        raf.getFD().sync();
    }

    private void ringErase(long position, long length) throws IOException {
        while (length > 0) {
            int chunk = (int) min(length, ZEROES.length);
            ringWrite(position, ZEROES, 0, chunk);
            length -= chunk;
            position += chunk;
        }
        raf.getFD().sync();
    }

    /**
     * Reads count bytes into buffer from file. Wraps if necessary.
     *
     * @param position in file to read from
     * @param buffer   to read into
     * @param count    # of bytes to read
     */
    private void ringRead(long position, byte[] buffer, int count) throws IOException {
        if (position + count <= stackFileLength) {
            raf.seek(position);
            raf.readFully(buffer, 0, count);
        } else {
            // The read overlaps the EOF.
            // # of bytes to read before the EOF. Guaranteed to be less than Integer.MAX_VALUE.
            int beforeEof = (int) (stackFileLength - position);
            raf.seek(position);
            raf.readFully(buffer, 0, beforeEof);
            raf.seek(stackHeaderLength);
            raf.readFully(buffer, beforeEof, count - beforeEof);
        }
    }

    /**
     * Adds an element to the end of the queue.
     *
     * @param data to copy bytes from
     */
    public void add(byte[] data) throws IOException {
        add(data, 0, data.length);
    }

    /**
     * Adds an element to the end of the queue.
     *
     * @param data   to copy bytes from
     * @param offset to start from in reusableHeaderBuffer
     * @param count  number of bytes to copy
     * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code count < 0}, or if {@code
     *                                   offset + count} is bigger than the length of {@code reusableHeaderBuffer}.
     */
    public void add(byte[] data, int offset, int count) throws IOException {
        if (data == null) {
            throw new NullPointerException("data == null");
        }
        if ((offset | count) < 0 || count > data.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        if (closed) throw new IllegalStateException("closed");

        expandIfNecessary(count);

        // Insert a new element after the current top element.
        boolean wasEmpty = isEmpty();
        long stride = Element.HEADER_LENGTH + count;
        long position = wasEmpty ? stackHeaderLength + stride
                : (top.position + stride);
        Element newTop = new Element(position, count);

        // Write data.
        ringWrite(position - stride, data, offset, count);

        writeInt(reusableHeaderBuffer, 0, count);
        ringWrite(position - Element.HEADER_LENGTH, reusableHeaderBuffer, 0, Element.HEADER_LENGTH);
        // Commit the addition. If wasEmpty, top == bottom.

        long firstPosition = wasEmpty ? position : bottom.position;

        writeHeader(stackFileLength, elementCount + 1, firstPosition, position);

        top = newTop;

        elementCount++;

        modCount++;

        if (wasEmpty) bottom = top; // bottom element
    }

    public long usedBytes() {
        if (elementCount == 0) return stackHeaderLength;

        if (top.position >= bottom.position) {
            // Contiguous queue.
            return (bottom.position - top.position)   // all but bottom entry
                    + Element.HEADER_LENGTH + bottom.length // bottom entry
                    + stackHeaderLength;
        } else {
            // tail < head. The queue wraps.
            return bottom.position                      // reusableHeaderBuffer front + header
                    + Element.HEADER_LENGTH + bottom.length // bottom entry
                    + stackFileLength - top.position;        // reusableHeaderBuffer end
        }
    }

    private long remainingBytes() {
        return stackFileLength - usedBytes();
    }

    /**
     * Returns true if this queue contains no entries.
     */
    public boolean isEmpty() {
        return elementCount == 0;
    }

    /**
     * If necessary, expands the file to accommodate an additional element of the given length.
     *
     * @param dataLength length of data being added
     */
    private void expandIfNecessary(long dataLength) throws IOException {
        long elementLength = Element.HEADER_LENGTH + dataLength;
        long remainingBytes = remainingBytes();
        if (remainingBytes >= elementLength) return;

        // Expand.
        long previousLength = stackFileLength;
        long newLength;
        // Double the length until we can fit the new data.
        do {
            remainingBytes += previousLength;
            newLength = previousLength << 1;
            previousLength = newLength;
        } while (remainingBytes < elementLength);

        setLength(newLength);

        // Calculate the position of the tail end of the data in the ring reusableHeaderBuffer
        long endOfLastElement = (bottom.position + Element.HEADER_LENGTH + bottom.length);
        long count = 0;
        // If the reusableHeaderBuffer is split, we need to make it contiguous
        if (endOfLastElement <= top.position) {
            FileChannel channel = raf.getChannel();
            channel.position(stackFileLength); // destination position
            count = endOfLastElement - stackHeaderLength;
            if (channel.transferTo(stackHeaderLength, count, channel) != count) {
                throw new AssertionError("Copied insufficient number of bytes!");
            }
        }

        // Commit the expansion.
        if (bottom.position < top.position) {
            long newLastPosition = stackFileLength + bottom.position - stackHeaderLength;
            writeHeader(newLength, elementCount, top.position, newLastPosition);
            bottom = new Element(newLastPosition, bottom.length);
        } else {
            writeHeader(newLength, elementCount, top.position, bottom.position);
        }

        stackFileLength = newLength;

        if (zero) {
            ringErase(stackHeaderLength, count);
        }
    }

    /**
     * Sets the length of the file.
     */
    private void setLength(long newLength) throws IOException {
        // Set new file length (considered metadata) and sync it to storage.
        raf.setLength(newLength);
        raf.getChannel().force(true);
    }

    /**
     * @param element
     * @return
     * @throws IOException
     */
    private byte[] getData(Element element) throws IOException {
        if (closed) throw new IllegalStateException("closed");
        if (isEmpty()) return null;
        int length = element.length;
        byte[] data = new byte[length];
        ringRead(element.position - Element.HEADER_LENGTH - length, data, length);
        return data;
    }

    /**
     *  Only reads the top element on the stack.
     *
     * @return The top value on the stack.
     * @throws IOException
     */
    @NotNull
    public byte[] peek() throws IOException {
        return getData(top);
    }

    /**
     * @return The bottom of the stack.
     * @throws IOException
     */
    @NotNull
    public byte[] readBottom() throws IOException {
        return getData(bottom);
    }

    /**
     * Returns an iterator over elements in this StackFile.
     *
     * <p>The iterator disallows modifications to be made to the StackFile during iteration. Removing
     * elements from the head of the StackFile is permitted during iteration using
     * {@link Iterator#remove()}.
     *
     * <p>The iterator may throw an unchecked {@link IOException} during {@link Iterator#next()}
     * or {@link Iterator#remove()}.
     */
    @Override
    public Iterator<byte[]> iterator() {
        return new ElementIterator();
    }

    /**
     * Returns the number of elements in this queue.
     */
    public int size() {
        return elementCount;
    }

    /**
     * Removes the eldest element.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    public void remove() throws IOException {
        remove(1);
    }

    /**
     * Removes the eldest {@code n} elements.
     *
     * @throws NoSuchElementException if the queue is empty
     */
    private void remove(int n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("Cannot remove negative (" + n + ") number of elements.");
        }
        if (n == 0) {
            return;
        }
        if (n == elementCount) {
            clear();
            return;
        }
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        if (n > elementCount) {
            throw new IllegalArgumentException(
                    "Cannot remove more elements (" + n + ") than present in queue (" + elementCount + ").");
        }

        long eraseTotalLength = 0;

        // Read the position and length of the new top element.
        long newTopPosition = top.position;
        int newTopLength = top.length;
        for (int i = 0; i < n; i++) {
            eraseTotalLength += Element.HEADER_LENGTH + newTopLength;
            newTopPosition = (newTopPosition - Element.HEADER_LENGTH);
            ringRead(newTopPosition, reusableHeaderBuffer, Element.HEADER_LENGTH);
            newTopLength = readInt(reusableHeaderBuffer, 0);
            newTopPosition -= newTopLength;
        }

        // Commit the header.
        writeHeader(stackFileLength, elementCount - n, top.position, newTopPosition);
        elementCount -= n;
        modCount++;
        top.length = newTopLength;
        top.position = newTopPosition;

        if (zero) {
//            ringErase(top.length + top.position, eraseTotalLength);
        }
    }

    /**
     * Clears this queue. Truncates the file to the initial size.
     */
    public void clear() throws IOException {
        if (closed) throw new IllegalStateException("closed");

        // Commit the header.
        writeHeader(INITIAL_LENGTH, 0, 0, 0);

        if (zero) {
            // Zero out data.
            raf.seek(stackHeaderLength);
            raf.write(ZEROES, 0, INITIAL_LENGTH - stackHeaderLength);
        }

        elementCount = 0;
        top = Element.NULL;
        bottom = Element.NULL;
        if (stackFileLength > INITIAL_LENGTH) setLength(INITIAL_LENGTH);
        stackFileLength = INITIAL_LENGTH;
        modCount++;
    }

    /**
     * The underlying {@link File} backing this queue.
     */
    public File file() {
        return stackFile;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        raf.getFD().sync();
        raf.close();
    }

    @Override
    public String toString() {
        return "StackFile{"
                + "file=" + stackFile
                + ", zero=" + zero
                + ", length=" + stackFileLength
                + ", size=" + elementCount
                + ", top=" + top
                + ", bottom=" + bottom
                + '}';
    }

    /**
     * A pointer to an element.
     */
    static class Element {
        static final Element NULL = new Element(0, 0);

        /**
         * Length of element header in bytes.
         */
        static final int HEADER_LENGTH = 4;

        /**
         * Position in file.
         */
        long position;

        /**
         * The length of the data.
         */
        int length;

        /**
         * Constructs a new element.
         *
         * @param position within file
         * @param length   of data
         */
        Element(long position, int length) {
            this.position = position;
            this.length = length;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName()
                    + "[position=" + position
                    + ", length=" + length
                    + "]";
        }
    }

    /**
     * Fluent API for creating {@link StackFile} instances.
     */
    public static final class StackBuilder {
        final File file;
        boolean zero = true;
        boolean forceLegacy = false;

        /**
         * Start constructing a new queue backed by the given file.
         */
        public StackBuilder(File file) {
            if (file == null) {
                throw new NullPointerException("file == null");
            }
            this.file = file;
        }

        /**
         * When true, removing an element will also overwrite data with zero bytes.
         */
        public StackBuilder zero(boolean zero) {
            this.zero = zero;
            return this;
        }

        /**
         * When true, only the legacy (Tape 1.x) format will be used.
         */
        public StackBuilder forceLegacy(boolean forceLegacy) {
            this.forceLegacy = forceLegacy;
            return this;
        }

        /**
         * Constructs a new queue backed by the given builder. Only one instance should access a given
         * file at a time.
         */
        public StackFile build() throws IOException {
            RandomAccessFile raf = initializeFromFile(file, forceLegacy);
            StackFile qf = null;
            try {
                qf = new StackFile(file, raf, zero, forceLegacy);
                return qf;
            } finally {
                if (qf == null) {
                    raf.close();
                }
            }
        }
    }

    private final class ElementIterator implements Iterator<byte[]> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int nextElementIndex = 0;
        /**
         * The {@link #modCount} value that the iterator believes that the backing StackFile should
         * have. If this expectation is violated, the iterator has detected concurrent modification.
         */
        int expectedModCount = modCount;
        /**
         * Position of element to be returned by subsequent call to next.
         */
        private long nextElementPosition = top.position;

        private ElementIterator() {
        }

        private void checkForComodification() {
            if (modCount != expectedModCount) throw new ConcurrentModificationException();
        }

        @Override
        public boolean hasNext() {
            if (closed) throw new IllegalStateException("closed");
            checkForComodification();
            return nextElementIndex != elementCount;
        }

        @Override
        public byte[] next() {
            if (closed) throw new IllegalStateException("closed");
            checkForComodification();
            if (isEmpty()) throw new NoSuchElementException();
            if (nextElementIndex >= elementCount) throw new NoSuchElementException();

            try {
                // Read the current element.
                Element current = readElement(nextElementPosition);
                byte[] buffer = new byte[current.length];
                nextElementPosition = (current.position + Element.HEADER_LENGTH);
                ringRead(nextElementPosition, buffer, current.length);

                // Update the pointer to the next element.
                nextElementPosition =
                        (current.position + Element.HEADER_LENGTH + current.length);
                nextElementIndex++;

                // Return the read element.
                return buffer;
            } catch (IOException e) {
                throw StackFile.<Error>getSneakyThrowable(e);
            }
        }

        @Override
        public void remove() {
            checkForComodification();

            if (isEmpty()) throw new NoSuchElementException();
            if (nextElementIndex != 1) {
                throw new UnsupportedOperationException("Removal is only permitted from the head.");
            }

            try {
                StackFile.this.remove();
            } catch (IOException e) {
                throw StackFile.<Error>getSneakyThrowable(e);
            }

            expectedModCount = modCount;
            nextElementIndex--;
        }
    }
}
