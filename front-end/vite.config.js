import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 3000,
    strictPort: true,
    host: true
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': [
            'react',
            'react-dom',
            'react-router-dom',
            'axios'
          ],
          'reportes': [
            './src/pages/reportes/ReportesOperativosPage.jsx',
            './src/pages/reportes/ReportesReservasPage.jsx',
            './src/pages/reportes/ReportesOcupacionPage.jsx',
            './src/pages/reportes/ReportesFinancierosPage.jsx',
            './src/pages/reportes/ReportesConsultasPage.jsx'
          ],
          'ui-components': [
            './src/components/ui/'
          ]
        }
      }
    },
    chunkSizeWarningLimit: 1000
  }
})
