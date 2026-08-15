-- Georreferenciamento por área: cada imóvel ganha um polígono retangular guardado como WKT em
-- SRID 31982 (SIRGAS 2000 / UTM 22S, metros). O cálculo (projeção 4326->31982 e montagem do
-- retângulo) e a verificação de sobreposição são feitos na aplicação (proj4j + JTS), sem depender
-- da extensão PostGIS instalada. Os imóveis já existentes são preenchidos no boot (backfill).
ALTER TABLE imovel ADD COLUMN poligono VARCHAR(1000);
