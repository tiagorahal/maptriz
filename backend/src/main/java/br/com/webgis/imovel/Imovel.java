package br.com.webgis.imovel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "imovel")
@Getter
@Setter
public class Imovel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 120)
	private String proprietario;

	@Column(length = 120)
	private String municipio;

	@Column(length = 2)
	private String uf;

	@Column(length = 100)
	private String bairro;

	@Column(length = 150)
	private String rua;

	@Column(length = 10)
	private String numero;

	@Column(precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(precision = 10, scale = 7)
	private BigDecimal longitude;

	@Column(name = "area_m2", precision = 12, scale = 2)
	private BigDecimal areaM2;

	private boolean ativo;

	@Column(name = "criado_em")
	private OffsetDateTime criadoEm;

	@Column(name = "atualizado_em")
	private OffsetDateTime atualizadoEm;

	@PrePersist
	void aoCriar() {
		this.criadoEm = OffsetDateTime.now();
		this.atualizadoEm = this.criadoEm;
	}

	@PreUpdate
	void aoAtualizar() {
		this.atualizadoEm = OffsetDateTime.now();
	}
}
