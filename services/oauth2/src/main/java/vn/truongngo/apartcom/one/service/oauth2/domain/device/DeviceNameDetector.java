package vn.truongngo.apartcom.one.service.oauth2.domain.device;

public interface DeviceNameDetector {
    DeviceName detect(String userAgent);
}
