package sdProject.dao;

import sdProject.models.Curso;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {
    private Connection conn;
    public CursoDAO(Connection conn) { this.conn = conn; }

    public void salvar(Curso curso) throws SQLException {
        String sql = "INSERT INTO curso (nome) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, curso.getNome());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    curso.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public Curso findById(int id) throws SQLException {
        String sql = "SELECT * FROM curso WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Curso curso = new Curso();
                    curso.setId(rs.getInt("id"));
                    curso.setNome(rs.getString("nome"));
                    return curso;
                }
            }
        }
        return null;
    }

    public List<Curso> listarTodos() throws SQLException {
        List<Curso> cursos = new ArrayList<>();
        String sql = "SELECT * FROM curso ORDER BY nome";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Curso curso = new Curso();
                curso.setId(rs.getInt("id"));
                curso.setNome(rs.getString("nome"));
                cursos.add(curso);
            }
        }
        return cursos;
    }

    public boolean atualizar(Curso curso) throws SQLException {
        String sql = "UPDATE curso SET nome = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, curso.getNome());
            stmt.setInt(2, curso.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM curso WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
}