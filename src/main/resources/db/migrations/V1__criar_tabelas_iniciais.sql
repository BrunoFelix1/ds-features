CREATE TABLE curso (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

CREATE TABLE disciplina (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL
);

CREATE TABLE curso_disciplina (
    curso_id INTEGER NOT NULL REFERENCES curso(id),
    disciplina_id INTEGER NOT NULL REFERENCES disciplina(id),
    PRIMARY KEY (curso_id, disciplina_id)
);

CREATE TABLE aluno (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL,
    curso_id INTEGER NOT NULL REFERENCES curso(id)
);

CREATE TABLE matricula (
    id SERIAL PRIMARY KEY,
    aluno_id INTEGER NOT NULL REFERENCES aluno(id),
    disciplina_id INTEGER NOT NULL REFERENCES disciplina(id),
    nota NUMERIC(4,2),
    UNIQUE(aluno_id, disciplina_id)
);
