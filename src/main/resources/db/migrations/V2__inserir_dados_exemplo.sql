-- Dados de exemplo para testes

-- Inserção de Cursos
INSERT INTO curso (nome) VALUES ('Ciência da Computação');
INSERT INTO curso (nome) VALUES ('Engenharia de Software');
INSERT INTO curso (nome) VALUES ('Sistemas de Informação');
INSERT INTO curso (nome) VALUES ('Análise e Desenvolvimento de Sistemas');

-- Inserção de Disciplinas
INSERT INTO disciplina (nome) VALUES ('Programação Orientada a Objetos');
INSERT INTO disciplina (nome) VALUES ('Banco de Dados');
INSERT INTO disciplina (nome) VALUES ('Sistemas Distribuídos');
INSERT INTO disciplina (nome) VALUES ('Inteligência Artificial');
INSERT INTO disciplina (nome) VALUES ('Redes de Computadores');
INSERT INTO disciplina (nome) VALUES ('Engenharia de Requisitos');

-- Relacionamento entre Cursos e Disciplinas (N:N)
-- Ciência da Computação
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (1, 1);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (1, 2);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (1, 3);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (1, 4);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (1, 5);

-- Engenharia de Software
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (2, 1);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (2, 2);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (2, 6);

-- Sistemas de Informação
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (3, 1);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (3, 2);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (3, 5);

-- ADS
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (4, 1);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (4, 2);
INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (4, 6);

-- Inserção de Alunos
INSERT INTO aluno (nome, cpf, matricula, ativo, curso_id) VALUES ('João Silva', '123.456.789-00', '20220001', true, 1);
INSERT INTO aluno (nome, cpf, matricula, ativo, curso_id) VALUES ('Maria Oliveira', '234.567.890-11', '20220002', true, 1);
INSERT INTO aluno (nome, cpf, matricula, ativo, curso_id) VALUES ('Pedro Santos', '345.678.901-22', '20220003', true, 2);
INSERT INTO aluno (nome, cpf, matricula, ativo, curso_id) VALUES ('Ana Costa', '456.789.012-33', '20220004', true, 3);
INSERT INTO aluno (nome, cpf, matricula, ativo, curso_id) VALUES ('Carlos Souza', '567.890.123-44', '20220005', false, 4);

-- Inserção de Matrículas
-- João Silva matriculado em POO com nota 8.5
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (1, 1, 8.5);
-- João Silva matriculado em BD com nota 7.8
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (1, 2, 7.8);
-- João Silva matriculado em SD sem nota ainda
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (1, 3, NULL);

-- Maria Oliveira matriculada em POO, BD e IA
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (2, 1, 9.2);
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (2, 2, 8.7);
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (2, 4, 9.5);

-- Pedro Santos matriculado em POO e Req
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (3, 1, 7.5);
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (3, 6, 8.3);

-- Ana Costa matriculada em BD e Redes
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (4, 2, 8.0);
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (4, 5, 7.5);

-- Carlos Souza matriculado em POO, mas está inativo
INSERT INTO matricula (aluno_id, disciplina_id, nota) VALUES (5, 1, 6.8);