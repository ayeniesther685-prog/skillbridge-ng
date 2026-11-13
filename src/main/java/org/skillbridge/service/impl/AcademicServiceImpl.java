package org.skillbridge.service.impl;

import org.skillbridge.dto.request.CourseCreateRequest;
import org.skillbridge.dto.request.DepartmentCreateRequest;
import org.skillbridge.dto.request.EnrollCourseRequest;
import org.skillbridge.dto.request.EnrollStudentRequest;
import org.skillbridge.dto.request.ProgramCreateRequest;
import org.skillbridge.dto.request.SemesterCreateRequest;
import org.skillbridge.dto.request.SessionCreateRequest;
import org.skillbridge.dto.response.AcademicDepartmentResponse;
import org.skillbridge.dto.response.AcademicProgramResponse;
import org.skillbridge.dto.response.AcademicSemesterResponse;
import org.skillbridge.dto.response.AcademicSessionResponse;
import org.skillbridge.dto.response.CourseResponse;
import org.skillbridge.dto.response.EnrollmentResponse;
import org.skillbridge.dto.response.StudentRecordResponse;
import org.skillbridge.entity.AcademicDepartment;
import org.skillbridge.entity.AcademicProgram;
import org.skillbridge.entity.AcademicSemester;
import org.skillbridge.entity.AcademicSession;
import org.skillbridge.entity.Course;
import org.skillbridge.entity.CourseEnrollment;
import org.skillbridge.entity.StudentRecord;
import org.skillbridge.entity.User;
import org.skillbridge.enums.EnrollmentStatus;
import org.skillbridge.enums.StudentStatus;
import org.skillbridge.exception.DuplicateResourceException;
import org.skillbridge.exception.ResourceNotFoundException;
import org.skillbridge.mapper.AcademicMapper;
import org.skillbridge.repository.AcademicDepartmentRepository;
import org.skillbridge.repository.AcademicProgramRepository;
import org.skillbridge.repository.AcademicSemesterRepository;
import org.skillbridge.repository.AcademicSessionRepository;
import org.skillbridge.repository.CourseEnrollmentRepository;
import org.skillbridge.repository.CourseRepository;
import org.skillbridge.repository.StudentRecordRepository;
import org.skillbridge.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
@Service
@Transactional(readOnly = true)
public class AcademicServiceImpl {

    private final AcademicProgramRepository programRepository;
    private final AcademicDepartmentRepository departmentRepository;
    private final AcademicSessionRepository sessionRepository;
    private final AcademicSemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final StudentRecordRepository studentRecordRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final AcademicMapper mapper;
    private final CurrentUser currentUser;

    public AcademicServiceImpl(AcademicProgramRepository programRepository,
                               AcademicDepartmentRepository departmentRepository,
                               AcademicSessionRepository sessionRepository,
                               AcademicSemesterRepository semesterRepository,
                               CourseRepository courseRepository,
                               StudentRecordRepository studentRecordRepository,
                               CourseEnrollmentRepository enrollmentRepository,
                               AcademicMapper mapper,
                               CurrentUser currentUser) {
        this.programRepository = programRepository;
        this.departmentRepository = departmentRepository;
        this.sessionRepository = sessionRepository;
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.studentRecordRepository = studentRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    public List<AcademicProgramResponse> listPrograms() {
        return programRepository.findByActiveTrueOrderByNameAsc()
                .stream().map(mapper::toProgramResponse).toList();
    }

    public List<AcademicDepartmentResponse> listDepartments(Long programId) {
        return departmentRepository.findByProgramIdOrderByNameAsc(programId)
                .stream().map(mapper::toDepartmentResponse).toList();
    }

    public List<CourseResponse> listCourses(Long departmentId) {
        return courseRepository.findByDepartmentIdAndActiveTrueOrderByCodeAsc(departmentId)
                .stream().map(mapper::toCourseResponse).toList();
    }

    public List<AcademicSessionResponse> listSessions() {
        return sessionRepository.findAll().stream().map(mapper::toSessionResponse).toList();
    }

    public List<AcademicSemesterResponse> listSemesters(Long sessionId) {
        return semesterRepository.findBySessionIdOrderByStartDateAsc(sessionId)
                .stream().map(mapper::toSemesterResponse).toList();
    }

    @Transactional
    public StudentRecordResponse enrollMe(EnrollStudentRequest req) {
        User me = currentUser.require();
        if (studentRecordRepository.existsByUserId(me.getId())) {
            throw new DuplicateResourceException("User is already enrolled as a student");
        }
        AcademicProgram program = programRepository.findById(req.programId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));
        AcademicDepartment dept = null;
        if (req.departmentId() != null) {
            dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        }

        StudentRecord r = new StudentRecord();
        r.setUser(me);
        r.setStudentId(generateStudentId());
        r.setProgram(program);
        r.setDepartment(dept);
        r.setAdmissionDate(LocalDate.now());
        r.setStatus(StudentStatus.ACTIVE);

        return mapper.toStudentRecordResponse(studentRecordRepository.save(r));
    }

    public StudentRecordResponse getMyRecord() {
        User me = currentUser.require();
        return studentRecordRepository.findByUserId(me.getId())
                .map(mapper::toStudentRecordResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No student record for this user"));
    }

    public List<EnrollmentResponse> listMyEnrollments() {
        User me = currentUser.require();
        StudentRecord rec = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No student record"));
        return enrollmentRepository.findByStudentRecordIdOrderByEnrolledAtDesc(rec.getId())
                .stream().map(mapper::toEnrollmentResponse).toList();
    }

       @Transactional
    public EnrollmentResponse enrollInCourse(EnrollCourseRequest req) {
        User me = currentUser.require();
        StudentRecord rec = studentRecordRepository.findByUserId(me.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Enroll as a student first"));
        Course course = courseRepository.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        AcademicSemester semester = semesterRepository.findById(req.semesterId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));

        // ===== PREREQUISITE CHECK (new) =====
        Course prereq = course.getPrerequisiteCourse();
        if (prereq != null) {
            Optional<CourseEnrollment> prereqEnrollment =
                    enrollmentRepository.findByStudentRecordIdAndCourseId(
                            rec.getId(), prereq.getId());
            if (prereqEnrollment.isEmpty()
                    || prereqEnrollment.get().getStatus() != EnrollmentStatus.COMPLETED) {
                throw new IllegalStateException(
                        "You must complete " + prereq.getCode()
                                + " (" + prereq.getTitle() + ") before enrolling in "
                                + course.getCode());
            }
        }
        // ===== END PREREQUISITE CHECK =====

        if (enrollmentRepository.existsByStudentRecordIdAndCourseIdAndSemesterId(
                rec.getId(), course.getId(), semester.getId())) {
            throw new DuplicateResourceException("Already enrolled in this course for this semester");
        }

        CourseEnrollment e = new CourseEnrollment();
        e.setStudentRecord(rec);
        e.setCourse(course);
        e.setSemester(semester);
        e.setStatus(EnrollmentStatus.ENROLLED);
        return mapper.toEnrollmentResponse(enrollmentRepository.save(e));
    }

    @Transactional
    public AcademicProgramResponse createProgram(ProgramCreateRequest req) {
        if (programRepository.existsByCode(req.code())) {
            throw new DuplicateResourceException("Program code already exists");
        }
        AcademicProgram p = new AcademicProgram();
        p.setName(req.name());
        p.setCode(req.code());
        p.setDescription(req.description());
        p.setDurationMonths(req.durationMonths());
        return mapper.toProgramResponse(programRepository.save(p));
    }

    @Transactional
    public AcademicDepartmentResponse createDepartment(DepartmentCreateRequest req) {
        if (departmentRepository.existsByCode(req.code())) {
            throw new DuplicateResourceException("Department code already exists");
        }
        AcademicProgram program = programRepository.findById(req.programId())
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));
        AcademicDepartment d = new AcademicDepartment();
        d.setProgram(program);
        d.setName(req.name());
        d.setCode(req.code());
        d.setDescription(req.description());
        return mapper.toDepartmentResponse(departmentRepository.save(d));
    }

    @Transactional
    public AcademicSessionResponse createSession(SessionCreateRequest req) {
        if (sessionRepository.existsByName(req.name())) {
            throw new DuplicateResourceException("Session name already exists");
        }
        AcademicSession s = new AcademicSession();
        s.setName(req.name());
        s.setStartDate(req.startDate());
        s.setEndDate(req.endDate());
        s.setCurrent(Boolean.TRUE.equals(req.isCurrent()));
        return mapper.toSessionResponse(sessionRepository.save(s));
    }

    @Transactional
    public AcademicSemesterResponse createSemester(SemesterCreateRequest req) {
        AcademicSession session = sessionRepository.findById(req.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        AcademicSemester s = new AcademicSemester();
        s.setSession(session);
        s.setName(req.name());
        s.setStartDate(req.startDate());
        s.setEndDate(req.endDate());
        return mapper.toSemesterResponse(semesterRepository.save(s));
    }

    @Transactional
    public CourseResponse createCourse(CourseCreateRequest req) {
        if (courseRepository.existsByCode(req.code())) {
            throw new DuplicateResourceException("Course code already exists");
        }
        AcademicDepartment dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Course c = new Course();
        c.setDepartment(dept);
        c.setCode(req.code());
        c.setTitle(req.title());
        c.setDescription(req.description());
        c.setLevel(req.level());
        c.setCredits(req.credits() == null ? 3 : req.credits());
        c.setDurationHours(req.durationHours());
        return mapper.toCourseResponse(courseRepository.save(c));
    }

    private String generateStudentId() {
        long count = studentRecordRepository.count() + 1;
        return String.format("SB/%d/%04d", Year.now().getValue(), count);
    }
}