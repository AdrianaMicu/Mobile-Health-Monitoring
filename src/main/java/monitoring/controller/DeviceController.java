package monitoring.controller;

import monitoring.utils.IoTConnectionUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/deviceController" })
public class DeviceController {

	@RequestMapping({ "/listDevices" })
	public String listDevices() {

		return IoTConnectionUtils
				.excuteMethod(
						"GET",
						"https://internetofthings.ibmcloud.com/api/v0001/organizations/xyzkfq/devices",
						null);
	}
}