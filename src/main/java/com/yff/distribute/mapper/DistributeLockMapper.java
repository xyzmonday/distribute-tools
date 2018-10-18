package com.yff.distribute.mapper;


import com.yff.distribute.entity.DistributeLock;
import org.apache.ibatis.annotations.Param;

public interface DistributeLockMapper {

    /**
     * 根据锁名和锁的信息获取一个锁
     *
     * @param lockName
     * @param lockDesc
     * @return
     */
    DistributeLock findOne(@Param("lockName") String lockName, @Param("lockDesc") String lockDesc);

    /**
     * 获取重入锁
     *
     * @param methodLock
     * @return
     */
    Integer acquireReentryLock(DistributeLock methodLock);

    Integer releaseReentryLock(@Param("lockName") String lockName, @Param("lockDesc") String lockDesc);

    /**
     * 插入一条记录，并返回
     *
     * @param record
     * @return
     */
    Integer insertRecord(DistributeLock record);

    /**
     * 根据lockName删除锁
     *
     * @return
     */
    Integer deleteRecord(@Param("lockName") String lockName);


    DistributeLock getLockForUpdate(@Param("lockName") String lockName);


}
