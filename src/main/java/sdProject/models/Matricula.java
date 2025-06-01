package sdProject.models;

import jakarta.persistence.*;

@Entity
@Table(name = "matricula")
public class Matricula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "aluno_id", nullable = false)
    private int alunoId;

    @Column(name = "disciplina_id", nullable = false)
    private int disciplinaId;

    @Column
    private Double nota;

    public Matricula() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlunoId() { return alunoId; }
    public void setAlunoId(int alunoId) { this.alunoId = alunoId; }

    public int getDisciplinaId() { return disciplinaId; }
    public void setDisciplinaId(int disciplinaId) { this.disciplinaId = disciplinaId; }

    public Double getNota() { return nota; }
    public void setNota(Double nota) { this.nota = nota; }
}