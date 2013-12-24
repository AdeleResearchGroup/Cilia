# iCasa Trackers

iCasa platform provides two different mechanisms to be notified about platform changes: Listeners and Trackers. A _Tracker_ is a very useful and easy-to-use mechanism to be notified when devices or zones are added, removed or changed in iCasa platform. 

## LocatedDeviceTracker 

LocatedDeviceTracker allows clients of iCasa platform be notified when a device with some characteristics is added, modified or remove in the platform. These characteristics are passed as arguments in the constructor of a LocatedDeviceTracker constructor as presented in the next code snippet.

      public LocatedDeviceTracker(BundleContext context, final Class<? extends GenericDevice> clazz, LocatedDeviceTrackerCustomizer customizer, final String... mandatoryProperties)
      
Clients must provide the class of Device to be tracked; it can optionally specify a customizer and a list of properties that have to been present at tracked devices.

A customizer is useful to decide if a device must be added to the tracked list using the method addingDevice. The others methods are callbacks to notify clients when a tracked device has been added, modified or removed.

In the next code snippet is presented the LocatedDeviceTrackerCustomizer interface

      public interface LocatedDeviceTrackerCustomizer {

          /**
           * A device is being added to the Tracker object.
           * This method is called before a device which matched the search parameters of the Tracker object is added to it.
           * This method must return true to be tracked for this device object.
           * @param device the device being added to the Tracker object.
           * @return The service object to be tracked for the DeviceReference object or null if the DeviceReference object should not be tracked.
           */
          boolean addingDevice(LocatedDevice device);
          
          /**
           * A device tracked by the Tracker object has been added in the list.
           * This method is called when a device has been added in the managed list (after addingDevice) and if the device has not disappeared before during the callback.
           * @param device the added device
           */
          void addedDevice(LocatedDevice device);

          /**
           * Called when a device tracked by the Tracker object has been modified.
           * A tracked device is considered modified according to tracker configuration.
           *
           * @param device the changed device
           * @param propertyName name of the property that has changed
           * @param oldValue previous value of the property
           * @param newValue new value of the property
           */
          void modifiedDevice(LocatedDevice device, String propertyName, Object oldValue, Object newValue);

          void movedDevice(LocatedDevice device, Position oldPosition, Position newPosition);

          /**
           * A device tracked by the Tracker object has been removed.
           * This method is called after a device is no longer being tracked by the Tracker object.
           * @param device the removed device.
           */
          void removedDevice(LocatedDevice device);

      }
      
To start the tracking, the client must call the open method in the tracker instance, on the other hand, the close method stop the tracking.

## ZoneTracker

ZoneTracker allows clients of iCasa platform be notified when a zone with some characteristics is added, modified or remove in the platform. These characteristics are passed as arguments in the constructor of a ZoneTracker constructor as presented in the next code snippet.

	public ZoneTracker(BundleContext context, ZoneTrackerCustomizer customizer, final String... mandatoryVariables)
   
ZoneTracker allows clients of iCasa platform be notified when a zone with some characteristics is added, modified or remove in the platform. These characteristics are passed as arguments in the constructor of a ZoneTracker constructor as presented in the next code snippet.

Clients can optionally specify a customizer and a list of variables that have to been present at tracked zone.

A customizer is useful to decide if a zone must be added to the tracked list using the method addingZone. The others methods are callbacks to notify clients when a tracked zone has been added, modified or removed.

In the next code snippet is presented the ZoneTrackerCustomizer interface

      public interface ZoneTrackerCustomizer {
          /**
           * A zone is being added to the Tracker object.
           * This method is called before a zone which matched the search parameters of the Tracker object is added to it.
           * This method must return true to be tracked for this zone object.
           * @param zone the zone being added to the Tracker object.
           * @return true if the zone will be tracked, false if not.
           */
          boolean addingZone(Zone zone);

          /**
           * A device tracked by the Tracker object has been added in the list.
           * This method is called when a device has been added in the managed list (after addingDevice) and if the device has not disappeared before during the callback.
           * @param zone the added device
           */
          void addedZone(Zone zone);

          /**
           * Called when a zone tracked by the Tracker object has been modified.
           * A tracked zone is considered modified according to tracker configuration.
           *
           * @param zone the changed zone
           * @param variableName name of the property that has changed
           * @param oldValue previous value of the property
           * @param newValue new value of the property
           */
          void modifiedZone(Zone zone, String variableName, Object oldValue, Object newValue);

          /**
           *
           * @param zone
           * @param oldPosition
           * @param newPosition
           */
          void movedZone(Zone zone, Position oldPosition, Position newPosition);

          /**
           * Called when a zone tracked by the Tracker object has been resized.
           *
           * @param zone the resized zone
           */
          void resizedZone(Zone zone);

          /**
           * A zone tracked by the Tracker object has been removed.
           * This method is called after a zone is no longer being tracked by the Tracker object.
           * @param zone the removed zone.
           */
          void removedZone(Zone zone);
      }
      
To start the tracking, the client must call the open method in the tracker instance, on the other hand, the close method stop the tracking.