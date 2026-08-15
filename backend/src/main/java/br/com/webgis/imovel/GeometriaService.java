package br.com.webgis.imovel;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransform;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Geometria dos imóveis sem depender do PostGIS: projeta lat/long (WGS 84) para SRID 31982
 * (SIRGAS 2000 / UTM 22S) com proj4j e monta/compara os polígonos com JTS. O polígono é trafegado
 * como WKT.
 */
@Service
public class GeometriaService {

	private static final int SRID_31982 = 31982;

	private final CoordinateTransform wgs84ParaUtm;
	private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID_31982);

	public GeometriaService() {
		CRSFactory crs = new CRSFactory();
		CoordinateReferenceSystem wgs84 = crs.createFromParameters(
				"WGS84", "+proj=longlat +datum=WGS84 +no_defs");
		CoordinateReferenceSystem utm22s = crs.createFromParameters(
				"EPSG:31982",
				"+proj=utm +zone=22 +south +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
		this.wgs84ParaUtm = new CoordinateTransformFactory().createTransform(wgs84, utm22s);
	}

	/**
	 * Monta o retângulo (largura×comprimento, em metros) centrado no ponto lat/long, projetado
	 * para SRID 31982, e devolve o WKT.
	 */
	public String gerarPoligonoWkt(BigDecimal latitude, BigDecimal longitude,
			BigDecimal largura, BigDecimal comprimento) {
		ProjCoordinate centro = projetar(longitude.doubleValue(), latitude.doubleValue());

		double meiaLargura = largura.doubleValue() / 2.0;
		double meioComprimento = comprimento.doubleValue() / 2.0;
		double x = centro.x;
		double y = centro.y;

		Coordinate[] cantos = {
				new Coordinate(x - meiaLargura, y - meioComprimento),
				new Coordinate(x + meiaLargura, y - meioComprimento),
				new Coordinate(x + meiaLargura, y + meioComprimento),
				new Coordinate(x - meiaLargura, y + meioComprimento),
				new Coordinate(x - meiaLargura, y - meioComprimento),
		};
		Polygon poligono = geometryFactory.createPolygon(cantos);
		return new WKTWriter().write(poligono);
	}

	/** True se o polígono candidato (WKT) intersecta algum da lista de polígonos existentes (WKT). */
	public boolean intersectaAlgum(String candidatoWkt, List<String> existentesWkt) {
		Polygon candidato = lerWkt(candidatoWkt);
		for (String wkt : existentesWkt) {
			if (candidato.intersects(lerWkt(wkt))) {
				return true;
			}
		}
		return false;
	}

	/** Lê um WKT (SRID 31982) de volta para um polígono JTS. */
	public Polygon lerWkt(String wkt) {
		try {
			return (Polygon) new WKTReader(geometryFactory).read(wkt);
		} catch (ParseException e) {
			throw new IllegalStateException("WKT de polígono inválido: " + wkt, e);
		}
	}

	// O transform do proj4j não é thread-safe: sincroniza a projeção (é rápida).
	private synchronized ProjCoordinate projetar(double longitude, double latitude) {
		ProjCoordinate destino = new ProjCoordinate();
		wgs84ParaUtm.transform(new ProjCoordinate(longitude, latitude), destino);
		return destino;
	}
}
