import { AfterViewInit, Component, ElementRef, OnDestroy, inject, viewChild } from '@angular/core';
import * as L from 'leaflet';
import { ImovelService } from '../imovel.service';

@Component({
  selector: 'app-mapa',
  imports: [],
  templateUrl: './mapa.html',
  styleUrl: './mapa.scss',
})
export class Mapa implements AfterViewInit, OnDestroy {
  private service = inject(ImovelService);
  private readonly container = viewChild.required<ElementRef<HTMLDivElement>>('mapa');
  private map?: L.Map;

  ngAfterViewInit(): void {
    // Centro aproximado do Brasil como estado inicial (ajustado depois pelos pontos).
    this.map = L.map(this.container().nativeElement).setView([-15.78, -47.93], 4);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap',
    }).addTo(this.map);

    this.service.pontos().subscribe((pontos) => {
      const camadas: L.Layer[] = [];
      for (const p of pontos) {
        const popup = `<strong>${p.proprietario}</strong><br>${p.municipio}`;

        // Ponto central (sempre visível, mesmo com o mapa afastado).
        const centro = L.circleMarker([p.latitude, p.longitude], {
          radius: 6,
          color: '#c0392b',
          weight: 2,
          fillColor: '#e74c3c',
          fillOpacity: 0.85,
        }).bindPopup(popup);
        centro.addTo(this.map!);
        camadas.push(centro);

        // Polígono (a área real do imóvel); aparece ao dar zoom.
        if (p.poligono?.length) {
          const area = L.polygon(p.poligono as L.LatLngExpression[], {
            color: '#c0392b',
            weight: 1,
            fillColor: '#e74c3c',
            fillOpacity: 0.35,
          }).bindPopup(popup);
          area.addTo(this.map!);
          camadas.push(area);
        }
      }
      // Enquadra o mapa para mostrar todos os imóveis.
      if (camadas.length) {
        this.map!.fitBounds(L.featureGroup(camadas).getBounds().pad(0.2));
      }
    });
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }
}
