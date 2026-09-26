package com.gdgku.attendance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    public Attendance checkIn(@RequestBody Attendance request) {
        return attendanceService.checkIn(request);
    }

    @GetMapping
    public List<Attendance> getAttendances() {
        return attendanceService.getAttendances();
    }

    @GetMapping("/{id}")
    public Attendance getAttendance(@PathVariable Long id) {
        return attendanceService.getAttendanceById(id);
    }

    @GetMapping("/late-count")
    public long countLate() {
        return attendanceService.countLate();
    }

    @PutMapping("/{id}")
    public Attendance updateCheckInTime(@PathVariable Long id, @RequestBody Attendance request) {
        return attendanceService.updateCheckInTime(id, request);
    }
}
