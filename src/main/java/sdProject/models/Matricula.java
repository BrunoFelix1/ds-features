package sdProject.models;

import java.io.Serializable;


public class Matricula implements Serializable {
    private int id;
    private int alunoId;
    private int disciplinaId;
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