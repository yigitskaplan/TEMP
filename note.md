# TEMP

http://localhost:8080/h2-console
user admin
password admin-dev-password

------------------------------------

jdbc:h2:mem:kule
admin
admin

------------------------------------------------------

HTTP request
    ↓
Controller   ← the door: URLs, methods, JSON in/out
    ↓
Service      ← the brain: logic, conversion between shapes
    ↓
Repository   ← the SQL: findAll, save, delete
    ↓
Entity       ← the table: one class = one table, one object = one row
    ↓
database

------------------------------------------------------------------
DTOs sit off to the side, describing what the outside world is allowed to send and see.

1. Entity — KISI.java

The table, expressed as a Java class. @Table(name = "KISI") binds the class to the table; each field binds to a column; one instance of the class is one row. This is your SQLAlchemy model.

It's the only file that knows about database concerns — @Id marks the primary key, @GeneratedValue says the DB assigns it. The entity is internal: it never leaves the service layer, which is why DTOs exist.

2. Repository — KISIRepository.java

The data access layer, and the file that does the most with the least. It's an interface — no code inside — yet extending JpaRepository<KISI, Integer> gives you findAll(), findById(), save(), deleteById(), count(), working at runtime. Spring generates the implementation and writes the SQL.

Later you can add custom queries by just declaring a method name — findByDurum(Integer durum) — and Spring derives WHERE durum = ? from the name itself. Closest FastAPI analogue is your hand-written SQLAlchemy query functions, except you don't write them.

3. DTOs — KISIDtos.java

Two records defining the API's contract, and the reason your entity never touches the outside world:

KISIRequest — what a client may send. Note it has no id field: clients don't get to choose primary keys. @NotBlank on adi is what produced your 400.
KISIResponse — what you send back. Here you control exactly which columns are exposed.

These are your Pydantic models. The separation is a settled rule in your notes ("never bind entities to requests"), and it's a security property: if the entity were bound directly, a client could POST fields you never intended to be writable, and every future column would auto-leak into responses. DTOs make exposure a deliberate act.

4. Service — KISIService.java

The business logic layer, sitting between the controller and the repository. Its jobs: call the repository, and translate between the two shapes — KISIRequest → entity on the way in, entity → KISIResponse on the way out.

Right now it's thin, which is normal for a plain lookup table. It earns its keep later: soft-delete logic ("set durum to 0 rather than deleting"), multi-step operations, transaction boundaries, cross-table rules. Keeping it now means those land in an obvious place instead of bloating the controller. FastAPI tutorials often skip this layer; Spring convention keeps it.

5. Controller — KISIController.java

The HTTP door. @RequestMapping("/api/kisiler") sets the base path; @GetMapping/@PostMapping map methods to routes. It converts JSON ↔ Java (@RequestBody), triggers validation (@Valid), and calls the service. Your @app.get / @app.post.

The rule that keeps this clean: the controller should contain no logic. It receives, delegates, returns. If you ever find an if about business rules here, it belongs in the service.

Why five files instead of one

Each has a single reason to change. Rename a URL → controller only. Change a validation rule → DTO only. Add a column → entity only. Change soft-delete behavior → service only. The database swap you have coming (H2 → Postgres) touches none of them — that's config.

For the remaining lookups (LOKASYON, MOD, TIP, ORTAM) all five files are near-identical to KISI's; only names and the REST path change. DB_HOST adds columns (hostname, memory, cpu); IP, VERI_MERKEZI, NETWORK add one FK column each as a plain Integer per your settled rule. ENVANTER is the only genuinely different one, and it waits on the durum question.
