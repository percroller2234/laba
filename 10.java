import java.io.*;
import java.util.*;

public class ClassicIOCacheWithLimit {
    private Map<String, FileCacheEntry> cache = new HashMap<>();
    private int maxSize;

    class FileCacheEntry {
        String content;
        long lastReadTime;
        long lastModifiedTimeAtRead;
        
        FileCacheEntry(String content, long lastReadTime, long lastModifiedTimeAtRead) {
            this.content = content;
            this.lastReadTime = lastReadTime;
            this.lastModifiedTimeAtRead = lastModifiedTimeAtRead;
        }
    }

    public ClassicIOCacheWithLimit(int maxSize) {
        this.maxSize = maxSize;
    }

    public ClassicIOCacheWithLimit() {
        this(100);
    }

    public String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Файл не найден: " + filePath);
        }

        String absolutePath = file.getAbsolutePath();
        long currentModifiedTime = file.lastModified();
        FileCacheEntry cachedEntry = cache.get(absolutePath);

        if (cachedEntry != null && cachedEntry.lastModifiedTimeAtRead == currentModifiedTime) {
            cachedEntry.lastReadTime = System.currentTimeMillis();
            return cachedEntry.content;
        }

        if (cache.size() >= maxSize) {
            removeOldest();
        }

        String content = readFileContent(file);
        long currentTime = System.currentTimeMillis();
        cache.put(absolutePath, new FileCacheEntry(content, currentTime, currentModifiedTime));
        return content;
    }

    private void removeOldest() {
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (Map.Entry<String, FileCacheEntry> entry : cache.entrySet()) {
            if (entry.getValue().lastReadTime < oldestTime) {
                oldestTime = entry.getValue().lastReadTime;
                oldestKey = entry.getKey();
            }
        }
        
        if (oldestKey != null) {
            cache.remove(oldestKey);
        }
    }

    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    public void invalidate(String filePath) {
        cache.remove(new File(filePath).getAbsolutePath());
    }

    public void invalidateAll() {
        cache.clear();
    }

    public boolean isCached(String filePath) {
        return cache.containsKey(new File(filePath).getAbsolutePath());
    }

    public int getCachedFilesCount() {
        return cache.size();
    }

    public static void main(String[] args) {
        ClassicIOCacheWithLimit cache = new ClassicIOCacheWithLimit(2);
        
        try {
            // Создаем тестовые файлы
            FileWriter f1 = new FileWriter("test1.txt");
            f1.write("Файл 1");
            f1.close();
            
            FileWriter f2 = new FileWriter("test2.txt");
            f2.write("Файл 2");
            f2.close();

            // Тестируем
            System.out.println("Читаем первый раз:");
            String content1 = cache.readFile("test1.txt");
            System.out.println(content1);
            
            System.out.println("Читаем второй раз (из кэша):");
            String content2 = cache.readFile("test1.txt");
            System.out.println(content2);
            
            System.out.println("В кэше: " + cache.getCachedFilesCount() + " файлов");
            
            // Очистка
            cache.invalidate("test1.txt");
            new File("test1.txt").delete();
            new File("test2.txt").delete();
            
        } catch (IOException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }
}
