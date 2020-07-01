package org.kframe.hotldeploy;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class WatchController {

    @Resource
    private IWatchService watchService;

    @GetMapping("/test")
    public String test() {
        System.out.println(watchService.getClass());
        return "hello world !";
    }
}
