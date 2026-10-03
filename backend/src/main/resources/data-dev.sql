-- Development-only, idempotent reference data. No users or credentials are seeded.
INSERT INTO skills (name, category, description)
VALUES
    ('Java', 'Programming', 'Java programming language'),
    ('SQL', 'Database', 'Relational database querying'),
    ('Spring Boot', 'Framework', 'Spring Boot application development'),
    ('AWS', 'Cloud', 'Amazon Web Services fundamentals')
ON CONFLICT (name) DO NOTHING;

INSERT INTO institutions (name)
SELECT 'Demo University'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Demo University')
);

INSERT INTO institutions (name)
SELECT 'Indian Institute of Technology Bombay'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Indian Institute of Technology Bombay')
);

INSERT INTO institutions (name)
SELECT 'Indian Institute of Technology Delhi'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Indian Institute of Technology Delhi')
);

INSERT INTO institutions (name)
SELECT 'Indian Institute of Technology Madras'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Indian Institute of Technology Madras')
);

INSERT INTO institutions (name)
SELECT 'Indian Institute of Technology Kanpur'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Indian Institute of Technology Kanpur')
);

INSERT INTO institutions (name)
SELECT 'Indian Institute of Technology Kharagpur'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Indian Institute of Technology Kharagpur')
);

INSERT INTO institutions (name)
SELECT 'Indian Institute of Technology Roorkee'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Indian Institute of Technology Roorkee')
);

INSERT INTO institutions (name)
SELECT 'National Institute of Technology Tiruchirappalli'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('National Institute of Technology Tiruchirappalli')
);

INSERT INTO institutions (name)
SELECT 'Birla Institute of Technology and Science, Pilani'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Birla Institute of Technology and Science, Pilani')
);

INSERT INTO institutions (name)
SELECT 'Vellore Institute of Technology'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Vellore Institute of Technology')
);

INSERT INTO institutions (name)
SELECT 'Anna University'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Anna University')
);

INSERT INTO institutions (name)
SELECT 'SRM Institute of Science and Technology'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('SRM Institute of Science and Technology')
);

INSERT INTO institutions (name)
SELECT 'Jadavpur University'
WHERE NOT EXISTS (
    SELECT 1 FROM institutions WHERE lower(name) = lower('Jadavpur University')
);
