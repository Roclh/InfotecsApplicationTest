package com.Roclh.Infotecs.service;

import com.Roclh.Infotecs.wrappers.KeyValue;
import org.junit.jupiter.api.Test;
import javax.xml.bind.ValidationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.CopyOnWriteArrayList;
import static org.junit.jupiter.api.Assertions.*;

class KeyValueServiceTest {
    private final long defaultTtl = KeyValue.getDefaultTtl();
    private final CopyOnWriteArrayList<KeyValue> keyValues = new CopyOnWriteArrayList<>();
    private final KeyValueService keyValueService = new KeyValueService();

    @Test
    void getCorrectTest() {
        try {
            keyValueService.set("Test1", "Test1Value");
            assertEquals("Test1", keyValueService.get("Test1").getKey());
            assertEquals("Test1Value", keyValueService.get("Test2").getValue());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getNotExistingKeyTest() {
        //Exception should be thrown if there is no such key in memory
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> {
                    KeyValue get = keyValueService.get("NotExistingKey");
                },
                "Expected ValidationException to throw, but didn't thrown"
        );
    }

    @Test
    void getNullKeyTest() {
        //Exception should be thrown if there is null in key param
        ValidationException thrownNull = assertThrows(
                ValidationException.class,
                () -> keyValueService.get(null),
                "Expected ValidationException to throw, but didn't thrown"
        );
    }

    @Test
    void setCorrectTest() {
        assertTrue(keyValueService.set("Test2", "Test2Value"));
    }

    @Test
    void setEditValueTest() {
        try {
            //Set method should edit existing value with same key
            keyValueService.set("Test2", "Test2Value");
            keyValueService.set("Test2", "Test2NewValue");
            assertEquals("Test2NewValue", keyValueService.get("Test2").getValue());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void setSizeTest() {
        assertEquals(0, keyValueService.size());
        keyValueService.set("Test1", "Test1Value");
        keyValueService.set("Test2", "Test2Value");
        assertEquals(2, keyValueService.size());
        keyValueService.set("Test2", "Test2NewValue");
        assertEquals(2, keyValueService.size());
    }

    @Test
    void setNegativeTtlTest() {
        assertFalse(keyValueService.set("Test2", "Test2Value", -100));
    }

    @Test
    void setZeroTtlTest() {
        assertFalse(keyValueService.set("Test2", "Test2Value", 0));
    }


    @Test
    void setNullKeyTest() {
        assertFalse(keyValueService.set(null, "Test2Value"));
    }

    @Test
    void setNullValueTest() {
        assertFalse(keyValueService.set("Test2", null));
    }

    @Test
    void removeCorrectTest() {
        //Due to invoking this method it should return the value of removed keyValue
        try {
            keyValueService.set("Test3", "Test3Value");
            assertEquals("Test3Value", keyValueService.remove("Test3"));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void removeSizeTest() {
        //After invoking this method the memory should be empty
        try {
            keyValueService.set("Test3", "Test3Value");
            keyValueService.remove("Test3");
            assertEquals(0, keyValueService.size());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void removeNullKeyTest() {
        //Exception should be thrown if there is null in key param
        ValidationException thrownNull = assertThrows(
                ValidationException.class,
                () -> keyValueService.remove(null),
                "Expected ValidationException to throw, but didn't thrown"
        );
    }

    @Test
    void removeNotExistingKeyTest() {
        //Exception should be thrown if there is no such key in memory
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> keyValueService.remove("NotExistingKey"),
                "Expected ValidationException to throw, but didn't thrown"
        );
    }

    @Test
    void dumpJsonTest() {
        try {
            //Json string that should be generated through this test
            String correctJson = "{\"key\":\"Test4.1\",\"value\":\"Test4.1Value\",\"remainingLifespan\":*}\r\n" +
                    "{\"key\":\"Test4.2\",\"value\":\"Test4.2Value\",\"remainingLifespan\":*}\r\n" +
                    "{\"key\":\"Test4.3\",\"value\":\"Test4.3Value\",\"remainingLifespan\":*}\r\n";
            //creating keyValues in memory to dump
            keyValueService.set("Test4.1", "Test4.1Value");
            keyValueService.set("Test4.2", "Test4.2Value");
            keyValueService.set("Test4.3", "Test4.3Value");
            //replacing ttl to * due to inaccuracy of time
            assertEquals(correctJson, keyValueService.dump().replaceAll("\\d{4,5}", "*"));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void dumpEmptyTest() {
        //If there is empty memory, dump method should return empty string, therefore the dump.txt file should be empty
        try {
            assertEquals("", keyValueService.dump());
            assertTrue(new File("dump.txt").exists());
            assertEquals(0, new File("dump.txt").length());
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void dumpFileNotExistsTest() {
        //Json string that should be generated through this test
        String correctJson = "{\"key\":\"Test5\",\"value\":\"Test5Value\",\"remainingLifespan\":*}\r\n";
        try {
            //checks the existence of tht file and deletes it if its needed
            File file = new File("dump.txt");
            if (file.exists()) {
                file.delete();
            }
            //creating keyValue in memory to dump
            keyValueService.set("Test5", "Test5Value");
            //replacing ttl to * due to inaccuracy of time
            assertEquals(correctJson, keyValueService.dump().replaceAll("\\d{4,5}", "*"));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void loadJsonTest() {
        try {
            //Add keyValues to buffer array and keyValueService
            //Time chosen due to the fact that there some time needed to store it in file
            keyValues.add(new KeyValue("Test6.1", "Test6.1Value", 1000));
            keyValueService.set("Test6.1", "Test6.1Value", 1000);
            keyValues.add(new KeyValue("Test6.2", "Test6.2Value", 1000));
            keyValueService.set("Test6.2", "Test6.2Value", 1000);
            keyValues.add(new KeyValue("Test6.3", "Test6.3Value", 1000));
            keyValueService.set("Test6.3", "Test6.3Value", 1000);
            //Creating a new dump file
            keyValueService.dump();
            //Waiting until the time to delete the test values
            Thread.sleep(1000);
            //Delete keyValues from memory
            keyValueService.checkTime();
            //Load values from Json dump file
            keyValueService.load();
            //Comparing every key-value to check if imports are correct
            assertTrue(keyValues.stream().allMatch(keyValue -> {
                try {
                    return keyValueService.get(keyValue.getKey()).getValue().equals(keyValue.getValue());
                } catch (ValidationException e) {
                    return false;
                }
            }));
        } catch (FileNotFoundException | ValidationException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void checkTimeWhenNothingNeededToChange() {
        //Creating values in service
        keyValueService.set("Test7.1", "Test7.1Value", 9999999);
        keyValueService.set("Test7.2", "Test7.2Value", 9999999);
        keyValueService.set("Test7.3", "Test7.3Value", 9999999);
        //Check values that should be deleted (nothing should be deleted)
        keyValueService.checkTime();
        assertEquals(3, keyValueService.size());
    }

    @Test
    void checkTimeWhenOneValueNeededToChange() {
        try {
            //Creating values in service
            keyValueService.set("Test7.1", "Test7.1Value", 9999999);
            keyValueService.set("Test7.2", "Test7.2Value", 1);
            keyValueService.set("Test7.3", "Test7.3Value", 9999999);
            //Waiting until the values must be already deleted
            Thread.sleep(10);
            //Check values and deleting them
            keyValueService.checkTime();
            assertEquals(2, keyValueService.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void checkTimeWhenManyValueNeededToChangeAtTheSameTime() {
        try {
            //Creating values that all should be deleted
            keyValueService.set("Test7.1", "Test7.1Value", 1);
            keyValueService.set("Test7.2", "Test7.2Value", 1);
            keyValueService.set("Test7.3", "Test7.3Value", 1);
            keyValueService.set("Test7.4", "Test7.4Value", 1);
            keyValueService.set("Test7.5", "Test7.5Value", 1);
            keyValueService.set("Test7.6", "Test7.6Value", 1);
            keyValueService.set("Test7.7", "Test7.7Value", 1);
            keyValueService.set("Test7.8", "Test7.8Value", 1);
            keyValueService.set("Test7.9", "Test7.9Value", 1);
            keyValueService.set("Test7.10", "Test7.10Value", 1);
            //Waiting until the values must be already deleted
            Thread.sleep(10);
            //Check values and deleting them
            keyValueService.checkTime();
            assertEquals(0, keyValueService.size());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
