package org.skillbridge.mapper;

import org.skillbridge.dto.response.*;
import org.skillbridge.entity.*;
import org.springframework.stereotype.Component;

@Component
public class AcademicMapper {

    public AcademicProgramResponse toProgramResponse(AcademicProgram p) {
        return new AcademicProgramResponse(
                p.getId(), p.getName(), p.getCode(), p.getDescription(),
                p.getDurationMonths(), p.isActive());
    }

    public AcademicDepartmentResponse toDepartmentResponse(AcademicDepartment d) {
        return new AcademicDepartmentResponse(
                d.getId(), d.getProgram().getId(), d.getProgram().getName(),
                d.getName(), d.getCode(), d.getDescription());
    }

    public AcademicSessionResponse toSessionResponse(AcademicSession s) {
        return new AcademicSessionResponse(
                s.getId(), s.getName(), s.getStartDate(), s.getEndDate(), s.isCurrent());
    }

    public AcademicSemesterResponse toSemesterResponse(AcademicSemester s) {
        return new AcademicSemesterResponse(
                s.getId(), s.getSession().getId(), s.getSession().getName(),
                s.getName(), s.getStartDate(), s.getEndDate());
    }
public CourseResponse toCourseResponse(Course c) {
    Course prereq = c.getPrerequisiteCourse();
    return new CourseResponse(
            c.getId(), c.getDepartment().getId(), c.getDepartment().getName(),
            c.getCode(), c.getTitle(), c.getDescription(),
            c.getLevel(), c.getCredits(), c.getDurationHours(), c.isActive(),
            prereq == null ? null : prereq.getId(),
            prereq == null ? null : prereq.getCode(),
            prereq == null ? null : prereq.getTitle()
    );
}

    public StudentRecordResponse toStudentRecordResponse(StudentRecord r) {
        return new StudentRecordResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getEmail(),
                r.getStudentId(),
                r.getProgram().getId(),
                r.getProgram().getName(),
                r.getDepartment() == null ? null : r.getDepartment().getId(),
                r.getDepartment() == null ? null : r.getDepartment().getName(),
                r.getAdmissionDate(),
                r.getStatus()
        );
    }

    public EnrollmentResponse toEnrollmentResponse(CourseEnrollment e) {
        return new EnrollmentResponse(
                e.getId(),
                e.getCourse().getId(),
                e.getCourse().getCode(),
                e.getCourse().getTitle(),
                e.getSemester().getId(),
                e.getSemester().getName(),
                e.getStatus(),
                e.getFinalGrade(),
                e.getEnrolledAt()
        );
    }
}