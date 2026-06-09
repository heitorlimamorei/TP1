package aed3;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Lista invertida persistente baseada em dicionario sequencial e blocos
 * encadeados, conforme a estrutura disponibilizada na disciplina.
 */
public class ListaInvertida implements AutoCloseable {

    public static class ElementoLista implements Comparable<ElementoLista> {
        private final int id;
        private final float frequencia;

        public ElementoLista(int id, float frequencia) {
            this.id = id;
            this.frequencia = frequencia;
        }

        public int getId() {
            return id;
        }

        public float getFrequencia() {
            return frequencia;
        }

        @Override
        public int compareTo(ElementoLista other) {
            return Integer.compare(id, other.id);
        }
    }

    private class Bloco {
        private short quantidade;
        private final short quantidadeMaxima;
        private final ElementoLista[] elementos;
        private long proximo;

        private Bloco(int quantidadeMaxima) {
            this.quantidade = 0;
            this.quantidadeMaxima = (short) quantidadeMaxima;
            this.elementos = new ElementoLista[quantidadeMaxima];
            this.proximo = -1;
        }

        private byte[] toByteArray() throws IOException {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            DataOutputStream data = new DataOutputStream(output);
            data.writeShort(quantidade);
            for (int index = 0; index < quantidadeMaxima; index++) {
                ElementoLista elemento = elementos[index];
                data.writeInt(elemento == null ? -1 : elemento.getId());
                data.writeFloat(elemento == null ? 0 : elemento.getFrequencia());
            }
            data.writeLong(proximo);
            return output.toByteArray();
        }

        private void fromByteArray(byte[] bytes) throws IOException {
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(bytes));
            quantidade = data.readShort();
            for (int index = 0; index < quantidadeMaxima; index++) {
                int id = data.readInt();
                float frequencia = data.readFloat();
                elementos[index] = id < 0 ? null : new ElementoLista(id, frequencia);
            }
            proximo = data.readLong();
        }

        private boolean create(ElementoLista elemento) {
            if (full()) {
                return false;
            }
            int index = quantidade - 1;
            while (index >= 0 && elemento.compareTo(elementos[index]) < 0) {
                elementos[index + 1] = elementos[index];
                index--;
            }
            elementos[index + 1] = elemento;
            quantidade++;
            return true;
        }

        private boolean contains(int id) {
            return findIndex(id) >= 0;
        }

        private boolean delete(int id) {
            int index = findIndex(id);
            if (index < 0) {
                return false;
            }
            while (index < quantidade - 1) {
                elementos[index] = elementos[index + 1];
                index++;
            }
            elementos[quantidade - 1] = null;
            quantidade--;
            return true;
        }

        private int findIndex(int id) {
            int index = 0;
            while (index < quantidade && id > elementos[index].getId()) {
                index++;
            }
            return index < quantidade && id == elementos[index].getId() ? index : -1;
        }

        private ElementoLista[] list() {
            return Arrays.copyOf(elementos, quantidade);
        }

        private boolean full() {
            return quantidade == quantidadeMaxima;
        }

        private int size() {
            return Short.BYTES + quantidadeMaxima * (Integer.BYTES + Float.BYTES) + Long.BYTES;
        }
    }

    private final RandomAccessFile dictionaryFile;
    private final RandomAccessFile blockFile;
    private final int entriesPerBlock;

    public ListaInvertida(int entriesPerBlock, String dictionaryFileName, String blockFileName) throws Exception {
        if (entriesPerBlock <= 0) {
            throw new IllegalArgumentException("A quantidade de elementos por bloco deve ser positiva.");
        }
        createParentDirectory(dictionaryFileName);
        createParentDirectory(blockFileName);
        this.entriesPerBlock = entriesPerBlock;
        this.dictionaryFile = new RandomAccessFile(dictionaryFileName, "rw");
        this.blockFile = new RandomAccessFile(blockFileName, "rw");
    }

    public boolean create(String key, ElementoLista element) throws Exception {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("A chave da lista invertida é obrigatória.");
        }
        if (element == null || element.getId() <= 0) {
            throw new IllegalArgumentException("O elemento da lista invertida é inválido.");
        }
        if (!Float.isFinite(element.getFrequencia())
            || element.getFrequencia() <= 0
            || element.getFrequencia() > 1) {
            throw new IllegalArgumentException("A frequência deve estar entre 0 e 1.");
        }
        for (ElementoLista existing : read(key)) {
            if (existing.getId() == element.getId()) {
                return false;
            }
        }

        long address = findKeyAddress(key);
        if (address < 0) {
            Bloco block = new Bloco(entriesPerBlock);
            address = blockFile.length();
            writeBlock(address, block);
            dictionaryFile.seek(dictionaryFile.length());
            dictionaryFile.writeUTF(key);
            dictionaryFile.writeLong(address);
        }

        while (address >= 0) {
            Bloco block = readBlock(address);
            if (!block.full()) {
                block.create(element);
                writeBlock(address, block);
                return true;
            }

            if (block.proximo < 0) {
                Bloco nextBlock = new Bloco(entriesPerBlock);
                long nextAddress = blockFile.length();
                writeBlock(nextAddress, nextBlock);
                block.proximo = nextAddress;
                writeBlock(address, block);
            }
            address = block.proximo;
        }
        return false;
    }

    public ElementoLista[] read(String key) throws Exception {
        ArrayList<ElementoLista> result = new ArrayList<>();
        long address = findKeyAddress(key);
        while (address >= 0) {
            Bloco block = readBlock(address);
            result.addAll(Arrays.asList(block.list()));
            address = block.proximo;
        }
        result.sort(null);
        return result.toArray(new ElementoLista[0]);
    }

    public boolean delete(String key, int id) throws Exception {
        long address = findKeyAddress(key);
        while (address >= 0) {
            Bloco block = readBlock(address);
            if (block.contains(id)) {
                block.delete(id);
                writeBlock(address, block);
                return true;
            }
            address = block.proximo;
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        blockFile.close();
        dictionaryFile.close();
    }

    private long findKeyAddress(String key) throws IOException {
        dictionaryFile.seek(0);
        while (dictionaryFile.getFilePointer() < dictionaryFile.length()) {
            String existingKey = dictionaryFile.readUTF();
            long address = dictionaryFile.readLong();
            if (existingKey.equals(key)) {
                return address;
            }
        }
        return -1;
    }

    private Bloco readBlock(long address) throws Exception {
        Bloco block = new Bloco(entriesPerBlock);
        byte[] bytes = new byte[block.size()];
        blockFile.seek(address);
        blockFile.readFully(bytes);
        block.fromByteArray(bytes);
        return block;
    }

    private void writeBlock(long address, Bloco block) throws Exception {
        blockFile.seek(address);
        blockFile.write(block.toByteArray());
    }

    private void createParentDirectory(String fileName) throws IOException {
        Path parent = Path.of(fileName).toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
