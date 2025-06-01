# Documentação dos Endpoints

## Serviço de Matrícula

### Matricular Aluno
- **Action**: `matricular`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
  - `disciplinaId` (int, obrigatório): ID da disciplina
- **Retorno**:
```json
{
    "status": "success|error",
    "message": "Mensagem de sucesso ou erro",
    "data": {
        "id": "ID da matrícula",
        "alunoId": "ID do aluno",
        "disciplinaId": "ID da disciplina",
        "nota": "Nota (null se não houver)"
    }
}
```

### Cancelar Matrícula
- **Action**: `cancelar`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
  - `disciplinaId` (int, obrigatório): ID da disciplina
- **Retorno**:
```json
{
    "status": "success|error",
    "message": "Mensagem de sucesso ou erro",
    "data": true|false
}
```

### Listar Matrículas por Aluno
- **Action**: `listarPorAluno`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**:
```json
{
    "status": "success|error",
    "data": [
        {
            "id": "ID da matrícula",
            "alunoId": "ID do aluno",
            "disciplinaId": "ID da disciplina",
            "nota": "Nota (null se não houver)"
        }
    ]
}
```

### Listar Matrículas por Disciplina
- **Action**: `listarPorDisciplina`
- **Parâmetros**:
  - `disciplinaId` (int, obrigatório): ID da disciplina
- **Retorno**: Mesmo formato de listarPorAluno

### Verificar Matrícula
- **Action**: `verificar`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
  - `disciplinaId` (int, obrigatório): ID da disciplina
- **Retorno**:
```json
{
    "status": "success|error",
    "data": true|false
}
```

## Serviço de Notas

### Registrar Nota
- **Action**: `registrarNota`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
  - `disciplinaId` (int, obrigatório): ID da disciplina
  - `nota` (double, obrigatório): Nota do aluno (0-10)
- **Retorno**:
```json
{
    "status": "success|error",
    "message": "Mensagem de sucesso ou erro",
    "data": true|false
}
```

### Consultar Nota
- **Action**: `consultarNota`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
  - `disciplinaId` (int, obrigatório): ID da disciplina
- **Retorno**:
```json
{
    "status": "success|error",
    "data": {
        "alunoId": "ID do aluno",
        "disciplinaId": "ID da disciplina",
        "nota": "Nota do aluno"
    }
}
```

### Calcular Média do Aluno
- **Action**: `mediaAluno`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**:
```json
{
    "status": "success|error",
    "data": "Média do aluno (double)"
}
```

### Calcular Média da Disciplina
- **Action**: `mediaDisciplina`
- **Parâmetros**:
  - `disciplinaId` (int, obrigatório): ID da disciplina
- **Retorno**:
```json
{
    "status": "success|error",
    "data": "Média da disciplina (double)"
}
```

## Serviço de Histórico

### Gerar Histórico Completo
- **Action**: `historicoCompleto`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**:
```json
{
    "status": "success|error",
    "data": {
        "alunoId": "ID do aluno",
        "historico": {
            "disciplina_ID": {
                "id": "ID da disciplina",
                "nome": "Nome da disciplina",
                "nota": "Nota do aluno"
            }
        },
        "coeficienteRendimento": "CR do aluno"
    }
}
```

### Listar Disciplinas Aprovadas
- **Action**: `disciplinasAprovadas`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**:
```json
{
    "status": "success|error",
    "data": {
        "disciplina_ID": {
            "id": "ID da disciplina",
            "nome": "Nome da disciplina",
            "nota": "Nota do aluno"
        }
    }
}
```

### Listar Disciplinas Reprovadas
- **Action**: `disciplinasReprovadas`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**: Mesmo formato de disciplinasAprovadas

### Listar Disciplinas Em Curso
- **Action**: `disciplinasEmCurso`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**:
```json
{
    "status": "success|error",
    "data": [
        {
            "id": "ID da disciplina",
            "nome": "Nome da disciplina"
        }
    ]
}
```

### Calcular Coeficiente de Rendimento
- **Action**: `coeficienteRendimento`
- **Parâmetros**:
  - `alunoId` (int, obrigatório): ID do aluno
- **Retorno**:
```json
{
    "status": "success|error",
    "data": "CR do aluno (double)"
}
```
