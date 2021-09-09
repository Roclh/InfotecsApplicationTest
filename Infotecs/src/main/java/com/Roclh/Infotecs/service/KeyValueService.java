package com.Roclh.Infotecs.service;

import com.Roclh.Infotecs.wrappers.KeyValue;
import org.springframework.stereotype.Service;

import javax.xml.bind.ValidationException;
import java.io.*;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class KeyValueService {
    private final long defaultTtl = KeyValue.getDefaultTtl();
    private final CopyOnWriteArrayList<KeyValue> keyValues = new CopyOnWriteArrayList<>();

    public KeyValueService() {
    }

    /**
     * Method that check the existence of a KeyValue with specified key
     *
     * @param key Key of the KeyValue
     * @return True if exists, false if not
     */
    private boolean checkKey(String key) {
        return this.keyValues.stream().anyMatch(keyValue -> keyValue.getKey().equals(key));
    }

    /**
     * Method that find a KeyValue with specified key
     *
     * @param key Key of the KeyValue
     * @return KeyValue with specified key
     * @throws ValidationException when there is no such key in values
     */
    public KeyValue get(String key) throws ValidationException {
        if (checkKey(key)) {
            return keyValues.stream().filter(keyValue -> keyValue.getKey().equals(key)).findFirst().get();
        } else {
            throw new ValidationException("There is no such key in values");
        }
    }

    /**
     * Method that creates new or change already existed keyValue in memory
     *
     * @param key Specified key of a new KeyValue
     * @param value Specified value of a new KeyValue
     * @return True if it's added, false if not
     */
    public boolean set(String key, String value) {
        return set(key, value, defaultTtl);
    }

    /**
     * Method that creates new or change already existed keyValue in memory
     *
     * @param key Specified key of a new or already existed KeyValue
     * @param value Specified value of a new or already existed KeyValue
     * @param ttl Specified time of existence of a new or already existed KeyValue
     * @return True if it's added or edited, false if not
     */
    public boolean set(String key, String value, long ttl) {
        if(ttl<=0||key == null|| value == null || key.equals("")){
            return false;
        }
        if (checkKey(key)) {
            keyValues.stream().filter(keyValue -> keyValue.getKey().equals(key)).findFirst().get().setValue(value);
            keyValues.stream().filter(keyValue -> keyValue.getKey().equals(key)).findFirst().get().setDeathTime(ttl);
            return true;
        } else {
            return add(key, value, ttl);
        }
    }

    /**
     * Method that creates new keyValue in memory
     *
     * @param key Specified key of a new KeyValue
     * @param value Specified value of a new KeyValue
     * @param ttl Specified time of existence of a new KeyValue
     * @return True if it's added, false if not
     */
    private boolean add(String key, String value, long ttl) {
        return this.keyValues.add(new KeyValue(key, value, ttl));
    }

    /**
     * Method that creates new or change already existed keyValue in memory
     *
     * @param keyValue a keyValue that needed to be added in the memory
     * @return True if it's added, false if not
     */
    private boolean set(KeyValue keyValue) {
        return this.set(keyValue.getKey(), keyValue.getValue(), keyValue.getRemainingLifespan());
    }

    /**
     * Method that removes keyValue with specified key from memory
     *
     * @param key Specified key of a KeyValue
     * @return Value if its removed
     * @throws ValidationException if there is no such key in values
     */
    public String remove(String key) throws ValidationException {
        if (checkKey(key)) {
            String value = keyValues.stream().filter(keyValue -> keyValue.getKey().equals(key)).findFirst().get().getValue();
            keyValues.removeIf(keyValue -> keyValue.getKey().equals(key));
            return value;
        } else {
            throw new ValidationException("There is no such key in values");
        }
    }

    /**
     * Method that dumps existing memory in the dump.txt file
     *
     * @return Json String with all keyValues in it
     * @throws ValidationException if it impossible to create a new file
     */
    public String dump() throws ValidationException {
        File file = new File("dump.txt");
        try {
            if (file.exists()) {
                return getValues(file);
            } else {
                if (file.createNewFile()) {
                    return getValues(file);
                } else {
                    throw new IOException("Unable to create file");
                }
            }
        } catch (IOException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    /**
     * Method that loads into existing memory the dump.txt file
     *
     * @return true if it succeeded, else false
     * @throws FileNotFoundException if there hasn't been any dump yet
     */
    public boolean load() throws FileNotFoundException {
        File file = new File("dump.txt");
        if (file.exists()) {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                this.set(new KeyValue(scanner.nextLine()));
            }
            scanner.close();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Submethod for load() that get values from file
     *
     * @param file json file from where it takes new values
     * @return Json String with all keyValues in it
     * @throws IOException if it impossible to read a file
     */
    private String getValues(File file) throws IOException {
        StringBuilder response = new StringBuilder();
        FileWriter fileWriter = new FileWriter(file, false);
        for (KeyValue keyValue : keyValues) {
            fileWriter.write(keyValue.toString() + "\r\n");
            response.append(keyValue.toString() + "\r\n");
        }
        fileWriter.flush();
        return response.toString();
    }

    /**
     * Method that checks all the existing values if they needed to be existing
     * if they're not - removes them.
     */
    public void checkTime() {
        keyValues.removeIf(keyValue -> {
            if (keyValue.checkTime()) {
                System.out.println("Removing value with key: " + keyValue.getKey());
            }
            return keyValue.checkTime();
        });

    }

    public int size(){
        return keyValues.size();
    }
}
