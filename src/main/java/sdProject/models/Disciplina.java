package sdProject.models;
import java.io.Serializable;

public class Disciplina implements Serializable {
    private int id;
    private String nome;

    public Disciplina() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}