package com.infosys.ppm;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.infosys.ppm.timesheet.TimesheetManagementSystem;

@Controller
public class ViewController {
	@GetMapping("/pending-timesheets-view")
	public String pendingTimesheets(Model model) {
		model.addAttribute("header", "PENDING TIMESHEETS");
		model.addAttribute("timesheets", TimesheetManagementSystem.getTimesheetsPending());
		return "list-view";
	}

	@GetMapping("/approved-timesheets-view")
	public String approvedTimesheets(Model model) {
		model.addAttribute("header", "APPROVED TIMESHEETS");
		model.addAttribute("timesheets", TimesheetManagementSystem.getTimesheetsApproved());
		return "list-view";
	}

}
