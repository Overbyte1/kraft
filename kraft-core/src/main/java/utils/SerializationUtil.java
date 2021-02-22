package utils;

import java.io.*;

public class SerializationUtil {
    public static byte[] encodes(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        byte[] bytes = out.toByteArray();
        return bytes;
    }

    public static Object decode(byte[] bytes) throws IOException, ClassNotFoundException {
        // 对象返序列化
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream inn = new ObjectInputStream(in);
        Object obj = inn.readObject();
        return obj;
    }
}
