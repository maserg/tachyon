package tachyon;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import tachyon.thrift.ClientWorkerInfo;
import tachyon.thrift.NetAddress;

/**
 * The structure to store a worker's information.
 */
public class WorkerInfo {
  public final InetSocketAddress ADDRESS;
  private final long CAPACITY_BYTES;
  private final long START_TIME_MS;

  private long mId;
  private long mUsedBytes;
  private long mLastUpdatedTimeMs;
  private Set<Integer> mFiles; 

  public WorkerInfo(long id, InetSocketAddress address, long capacityBytes) {
    mId = id;
    ADDRESS = address;
    CAPACITY_BYTES = capacityBytes;
    START_TIME_MS = System.currentTimeMillis();

    mUsedBytes = 0;
    mFiles = new HashSet<Integer>();
    mLastUpdatedTimeMs = System.currentTimeMillis();
  }

  public synchronized boolean containPartition(long partition) {
    return mFiles.contains(partition);
  }

  public synchronized InetSocketAddress getAddress() {
    return ADDRESS;
  }

  public synchronized long getAvailableBytes() {
    return CAPACITY_BYTES - mUsedBytes;
  }

  public long getCapacityBytes() {
    return CAPACITY_BYTES;
  }

  public synchronized long getId() {
    return mId;
  }

  public synchronized long getLastUpdatedTimeMs() {
    return mLastUpdatedTimeMs;
  }

  public synchronized Set<Integer> getFiles() {
    return new HashSet<Integer>(mFiles);
  }

  public synchronized long getUsedBytes() {
    return mUsedBytes;
  }

  public synchronized boolean requestSpaceBytes(long requestSpaceBytes) {
    if (getAvailableBytes() < requestSpaceBytes) {
      return false;
    }

    mUsedBytes += requestSpaceBytes;
    return true;
  }

  public synchronized void returnUsedBytes(long returnUsedBytes) {
    mUsedBytes -= returnUsedBytes;
  }

  public synchronized void removeFile(int fileId) {
    mFiles.remove(fileId);
  }

  @Override
  public synchronized String toString() {
    StringBuilder sb = new StringBuilder("WorkerInfo(");
    sb.append(" ID: ").append(mId);
    sb.append(", ADDRESS: ").append(ADDRESS);
    sb.append(", TOTAL_BYTES: ").append(CAPACITY_BYTES);
    sb.append(", mUsedBytes: ").append(mUsedBytes);
    sb.append(", mAvailableBytes: ").append(CAPACITY_BYTES - mUsedBytes);
    sb.append(", mLastUpdatedTimeMs: ").append(mLastUpdatedTimeMs);
    sb.append(", mFiles: [ ");
    for (int file : mFiles) {
      sb.append(file).append(", ");
    }
    sb.append("] )");
    return sb.toString();
  }

  public synchronized void updateFile(boolean add, int fileId) {
    if (add) {
      mFiles.add(fileId);
    } else {
      mFiles.remove(fileId);
    }
  }

  public synchronized void updateFiles(boolean add, Collection<Integer> fileIds) {
    if (add) {
      mFiles.addAll(fileIds);
    } else {
      mFiles.removeAll(fileIds);
    }
  }

  public synchronized void updateId(long id) {
    mId = id;
  }

  public synchronized void updateLastUpdatedTimeMs() {
    mLastUpdatedTimeMs = System.currentTimeMillis();
  }

  public synchronized void updateUsedBytes(long usedBytes) {
    mUsedBytes = usedBytes;
  }

  public ClientWorkerInfo generateClientWorkerInfo() {
    ClientWorkerInfo ret = new ClientWorkerInfo();
    ret.id = mId;
    ret.address = new NetAddress(ADDRESS.getHostName(), ADDRESS.getPort());
    ret.lastContactSec = (int) ((CommonUtils.getCurrentMs() - mLastUpdatedTimeMs) / 1000);
    ret.state = "In Service";
    ret.capacityBytes = CAPACITY_BYTES;
    ret.usedBytes = mUsedBytes;
    ret.starttimeMs = START_TIME_MS;
    return ret;
  }
}