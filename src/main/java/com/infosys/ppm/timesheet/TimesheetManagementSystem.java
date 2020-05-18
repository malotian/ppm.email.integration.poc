package com.infosys.ppm.timesheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.fluttercode.datafactory.impl.DataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimesheetManagementSystem {

	private static final DataFactory df = new DataFactory();

	private static final Logger LOGGER = LoggerFactory.getLogger(TimesheetManagementSystem.class);

	public static ArrayList<Timesheet> getTimesheetsPending() {
		return timesheetsPending;
	}

	public static ArrayList<Timesheet> getTimesheetsApproved() {
		return timesheetsApproved;
	}

	static ArrayList<Timesheet> timesheetsPending = new ArrayList<>();
	static ArrayList<Timesheet> timesheetsApproved = new ArrayList<>();

	@GetMapping("pending-timesheets")
	public List<Timesheet> pendings() {
		return timesheetsPending;
	}

	@GetMapping("generate-timesheets")
	public List<Timesheet> generate() {
		List<Timesheet> pendings = new ArrayList<>();
		int n = df.getNumberBetween(1, 10);
		for (int i = 0; i < n; i++) {
			pendings.add(new Timesheet() //
					.withId(df.getNumberText(8)) //
					.withName(df.getFirstName() + " " + df.getLastName()) //
					.withHours(String.valueOf(df.getNumberBetween(1, 16))) //
					.withDate(new SimpleDateFormat("dd-mmm-yyy").format(df.getDate(new Date(), -10, 1))));
		}

		timesheetsPending.addAll(pendings);
		return pendings;
	}

	@GetMapping("approve-timesheet/{id}")
	public boolean approve(@PathVariable String id) {
		Optional<Timesheet> ts = timesheetsPending.stream().filter(t -> t.getId().equals(id)).findFirst();
		if (ts.isEmpty()) {
			LOGGER.error("error, while approving timesheet, since no pending timesheet with ID: {}", id);
			return false;
		}
		timesheetsPending.remove(ts.get());
		timesheetsApproved.add(ts.get());
		return true;
	}

	@GetMapping("approved-timesheets")
	public List<Timesheet> approved() {
		return timesheetsApproved;
	}

	@GetMapping("clear-approved-timesheets")
	public boolean clearApproved() {
		timesheetsApproved.clear();
		return true;
	}

}
