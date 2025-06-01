package sdProject.controllers;

import sdProject.models.Disciplina;
import sdProject.services.interfaces.HistoricoService;
import sdProject.services.impl.HistoricoServiceImpl;
import sdProject.dto.HistoricoDTO;
import sdProject.dto.ResponseDTO;

import java.util.List;
import java.util.Map;

public class HistoricoController {
    
    private final HistoricoService historicoService;
    
    public HistoricoController() {
        this.historicoService = new HistoricoServiceImpl();
    }
    
    public ResponseDTO<HistoricoDTO> gerarHistoricoCompleto(int alunoId) {
        try {
            Map<Disciplina, Double> historico = historicoService.gerarHistoricoCompleto(alunoId);
            HistoricoDTO dto = new HistoricoDTO();
            dto.setAlunoId(alunoId);
            dto.setHistorico(historico);
            dto.setCoeficienteRendimento(historicoService.calcularCoeficienteRendimento(alunoId));
            return ResponseDTO.success(dto);
        } catch (IllegalArgumentException e) {
            return ResponseDTO.error(e.getMessage());
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao processar histórico: " + e.getMessage());
        }
    }
    
    public ResponseDTO<Map<Disciplina, Double>> listarDisciplinasAprovadas(int alunoId) {
        try {
            Map<Disciplina, Double> disciplinasAprovadas = historicoService.listarDisciplinasAprovadas(alunoId);
            return ResponseDTO.success(disciplinasAprovadas);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao listar disciplinas aprovadas: " + e.getMessage());
        }
    }
    
    public ResponseDTO<Map<Disciplina, Double>> listarDisciplinasReprovadas(int alunoId) {
        try {
            Map<Disciplina, Double> disciplinasReprovadas = historicoService.listarDisciplinasReprovadas(alunoId);
            return ResponseDTO.success(disciplinasReprovadas);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao listar disciplinas reprovadas: " + e.getMessage());
        }
    }
    
    public ResponseDTO<List<Disciplina>> listarDisciplinasEmCurso(int alunoId) {
        try {
            List<Disciplina> disciplinasEmCurso = historicoService.listarDisciplinasEmCurso(alunoId);
            return ResponseDTO.success(disciplinasEmCurso);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao listar disciplinas em curso: " + e.getMessage());
        }
    }
    
    public ResponseDTO<Double> calcularCoeficienteRendimento(int alunoId) {
        try {
            Double coeficiente = historicoService.calcularCoeficienteRendimento(alunoId);
            return ResponseDTO.success(coeficiente);
        } catch (Exception e) {
            return ResponseDTO.error("Erro ao calcular coeficiente: " + e.getMessage());
        }
    }
}
