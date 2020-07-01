package org.kframe.hotldeploy;

import java.util.List;

public interface IWatchService {

    /**
     * 发布服务
     * @param serviceName
     * @return
     */
    public int publishService(String serviceName);

    /**
     * 加载服务文件
     * @param fileData
     * @return
     */
    public List<ServiceInfo> loadServices(byte[] fileData);


    /**
     * 查询所有服务
     * @return
     */
    public List<ServiceInfo> queryService();
}
