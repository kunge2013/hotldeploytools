package org.kframe.hotldeploy;

import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class WatchService implements IWatchService {

    @Override
    public int publishService(String serviceName) {
        return 0;
    }

    @Override
    public List<ServiceInfo> loadServices(byte[] fileData) {
        return null;
    }

    @Override
    public List<ServiceInfo> queryService() {
        return null;
    }
}
