CREATE TABLE course_lessons (
    id                  BIGSERIAL       PRIMARY KEY,
    course_id           BIGINT          NOT NULL,
    title               VARCHAR(200)    NOT NULL,
    content             TEXT            NOT NULL,
    order_number        INTEGER         NOT NULL,
    duration_minutes    INTEGER         NOT NULL DEFAULT 30,
    learning_objectives VARCHAR(2000),
    video_url           VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lessons_course FOREIGN KEY (course_id)
        REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_lessons_course_order UNIQUE (course_id, order_number)
);

CREATE INDEX idx_lessons_course ON course_lessons (course_id, order_number);

INSERT INTO course_lessons (course_id, title, content, order_number, duration_minutes, learning_objectives)
VALUES
(
    (SELECT id FROM courses WHERE code = 'SWE101'),
    'Variables, Types, and Operators',
    E'# Variables, Types, and Operators\n\nIn Java, every variable has a type. The eight primitive types are:\n- byte, short, int, long (integers)\n- float, double (floating point)\n- char (a single Unicode character)\n- boolean (true or false)\n\n## Declaring variables\n\nint age = 25;\nString name = \"Ada\";\nboolean isActive = true;\n\nJava is statically typed, which means you must declare the type.\n\n## Operators\n\nint a = 10 + 5;\nint b = a * 2;\nboolean c = a > b;\n\n## Exercise\n\nDeclare three variables (your age, your name, whether you are a student) and print them to the console.\n',
    1, 45,
    'Understand primitive types, declare variables, use basic operators'
),
(
    (SELECT id FROM courses WHERE code = 'SWE101'),
    'Control Flow: Conditionals and Loops',
    E'# Control Flow\n\n## Conditionals\n\nif (age >= 18) {\n    System.out.println(\"Adult\");\n} else {\n    System.out.println(\"Minor\");\n}\n\nUse switch for multiple branches on a single value.\n\n## Loops\n\nfor (int i = 0; i < 5; i++) {\n    System.out.println(i);\n}\n\nint i = 0;\nwhile (i < 5) {\n    System.out.println(i);\n    i++;\n}\n\ndo-while runs the body at least once. break exits a loop. continue skips to the next iteration.\n\n## Exercise\n\nPrint the numbers 1-100, but for multiples of 3 print Fizz, for multiples of 5 print Buzz, and for multiples of both print FizzBuzz.\n',
    2, 60,
    'Write conditionals with if/else/switch, write for and while loops, understand break/continue'
),
(
    (SELECT id FROM courses WHERE code = 'SWE101'),
    'Classes and Objects',
    E'# Classes and Objects\n\nA class is a blueprint. An object is an instance.\n\npublic class Person {\n    private String name;\n    private int age;\n\n    public Person(String name, int age) {\n        this.name = name;\n        this.age = age;\n    }\n\n    public String greet() {\n        return \"Hello, I am \" + name;\n    }\n}\n\nPerson p = new Person(\"Ada\", 25);\nSystem.out.println(p.greet());\n\n## Key concepts\n\n- Encapsulation (private fields, public methods)\n- Inheritance (extends)\n- Polymorphism (overriding methods)\n- this refers to the current instance\n\n## Exercise\n\nCreate a BankAccount class with balance, deposit(amount), and withdraw(amount). Throw an exception if withdraw exceeds balance.\n',
    3, 75,
    'Define classes, instantiate objects, use constructors, encapsulate state with private fields'
),
(
    (SELECT id FROM courses WHERE code = 'SWE201'),
    'HTTP Fundamentals and REST Constraints',
    E'# HTTP Fundamentals\n\n## Methods\n\nGET: read a resource\nPOST: create a resource\nPUT: replace a resource\nPATCH: partially update a resource\nDELETE: remove a resource\n\n## Status codes\n\n- 2xx success (200 OK, 201 Created, 204 No Content)\n- 4xx client error (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict)\n- 5xx server error (500 Internal Server Error)\n\n## REST constraints\n\n1. Client-server: separation of concerns\n2. Stateless: each request contains all info needed\n3. Cacheable: responses declare cacheability\n4. Uniform interface: resources identified by URIs, manipulated via representations\n\n## Exercise\n\nDesign the endpoints for a Book Store API. What verbs and paths for creating a book, listing books, and updating a book price?\n',
    1, 60,
    'Understand HTTP methods, status codes, REST principles'
),
(
    (SELECT id FROM courses WHERE code = 'SWE201'),
    'Building Your First Spring Boot Controller',
    E'# Your First Controller\n\n@RestController\n@RequestMapping(\"/api/books\")\npublic class BookController {\n\n    private final BookService service;\n\n    public BookController(BookService service) {\n        this.service = service;\n    }\n\n    @GetMapping\n    public List<Book> list() {\n        return service.findAll();\n    }\n\n    @PostMapping\n    public ResponseEntity<Book> create(@RequestBody Book book) {\n        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(book));\n    }\n}\n\n## Anatomy\n\n- @RestController combines @Controller and @ResponseBody\n- @RequestMapping sets the base path for all handlers\n- @GetMapping and @PostMapping are method-level HTTP verbs\n- @PathVariable maps /books/{id}\n- @RequestParam maps ?sort=title\n- @RequestBody converts JSON to Java\n- ResponseEntity gives full control over status, headers, body\n\n## Exercise\n\nCreate a BookController with GET /api/books/{id}, PUT /api/books/{id}, and DELETE /api/books/{id}. Return 404 when the book does not exist.\n',
    2, 90,
    'Build a Spring Boot REST controller with GET and POST, understand annotations'
);