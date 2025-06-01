package sdProject.network.util;

import java.io.*;

public class SerializationUtils {
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream aos = new ObjectOutputStream(baos)){
            aos.writeObject(obj);

            return baos.toByteArray();
        }
    }

    public static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null || bytes.length == 0){
            throw new IOException("Não é possível desserializar arrays null ou vazios");
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais)){
            return ois.readObject();
        }
    }
}
