package sdProject.dao;

import sdProject.models.CursoDisciplina;
import sdProject.models.Curso;
import sdProject.models.Disciplina;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDisciplinaDAO {
    private Connection conn;
    
    public CursoDisciplinaDAO(Connection conn) { 
        this.conn = conn; 
    }

    public void salvar(CursoDisciplina cursoDisciplina) throws SQLException {
        String sql = "INSERT INTO curso_disciplina (curso_id, disciplina_id) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cursoDisciplina.getCursoId());
            stmt.setInt(2, cursoDisciplina.getDisciplinaId());
            stmt.executeUpdate();
        }
    }

    public boolean removerRelacionamento(int cursoId, int disciplinaId) throws SQLException {
        String sql = "DELETE FROM curso_disciplina WHERE curso_id = ? AND disciplina_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cursoId);
            stmt.setInt(2, disciplinaId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Disciplina> buscarDisciplinasPorCurso(int cursoId) throws SQLException {
        List<Disciplina> disciplinas = new ArrayList<>();
        String sql = "SELECT d.* FROM disciplina d " +
                     "INNER JOIN curso_disciplina cd ON d.id = cd.disciplina_id " +
                     "WHERE cd.curso_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cursoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Disciplina disciplina = new Disciplina();
                    disciplina.setId(rs.getInt("id"));
                    disciplina.setNome(rs.getString("nome"));
                    disciplinas.add(disciplina);
                }
            }
        }
        return disciplinas;
    }

    public List<Curso> buscarCursosPorDisciplina(int disciplinaId) throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT c.* FROM curso c " +
                     "INNER JOIN curso_disciplina cd ON c.id = cd.curso_id " +
                     "WHERE cd.disciplina_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, disciplinaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Curso curso = new Curso();
                    curso.setId(rs.getInt("id"));
                    curso.setNome(rs.getString("nome"));
                    cursos.add(curso);
                }
            }
        }
        return cursos;
    }
}