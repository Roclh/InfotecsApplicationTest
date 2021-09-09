package com.Roclh.Infotecs.beans;

import com.Roclh.Infotecs.service.KeyValueService;
import com.Roclh.Infotecs.wrappers.KeyValue;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import javax.xml.bind.ValidationException;
import java.io.FileNotFoundException;
import java.util.Optional;

@RestController
@RequestMapping("/main")
@EnableScheduling
public class MainController{

    private final KeyValueService keyValueService = new KeyValueService();

    /**
     * Method that realizes get request
     *
     * @param key Specified key of the keyValue
     * @return value of the specified keyValue
     * @throws ValidationException if there is no such key in memory
     */
    @GetMapping("/get")
    @ResponseBody
    public String get(@RequestParam(name="key") String key) throws ValidationException {
        System.out.println("Handling get request by key: " + key);
        return keyValueService.get(key).getValue();
    }

    /**
     * Method that realizes set request
     *
     * @param key Specified key of the keyValue
     * @param value of the specified keyValue
     * @param ttl Specified time of existence of a new KeyValue
     * @return True if it's created, False if not
     */
    @PostMapping("/set")
    @ResponseBody
    public boolean set(@RequestParam(name="key") String key, @RequestParam(name="value") String value, @RequestParam(name="ttl") Optional<Long> ttl){
        System.out.println("Handling set request: key:\""+key + "\", value:\""+value+"\", ttl:\""+ttl.orElseGet(KeyValue::getDefaultTtl)+"\"");
        return keyValueService.set(key, value, ttl.orElseGet(KeyValue::getDefaultTtl));
    }

    /**
     * Method that realizes remove request
     *
     * @param key Specified key of the keyValue
     * @return value of the removed keyValue
     * @throws ValidationException if there is no such key in memory
     */
    @DeleteMapping("/remove")
    @ResponseBody
    public String remove(@RequestParam(name="key") String key) throws ValidationException {
        System.out.println("Handling remove request: key:\""+key+"\"");
        return keyValueService.remove(key);
    }

    /**
     * Method that realizes load request
     *
     * @return true if it loaded
     * @throws FileNotFoundException if there hasn't been any dumps yet
     */
    @PostMapping("/load")
    @ResponseBody
    public boolean load() throws FileNotFoundException {
        System.out.println("Handling load request");
        return keyValueService.load();
    }

    /**
     * Method that realizes dump request
     *
     * @return Json string with all values if it dumped
     * @throws ValidationException if it's impossible to create a dump.txt file
     */
    @GetMapping("/dump")
    @ResponseBody
    public String dump() throws ValidationException {
        System.out.println("Handling dump request");
        return keyValueService.dump();
    }

    /**
     * Method that invokes time checker to remove them
     */
    @Scheduled(fixedRate = 1)
    public void check(){
        keyValueService.checkTime();
    }
}
