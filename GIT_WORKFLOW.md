# Git Workflow Simplificado - Para el Equipo

## 🎯 Objetivo
Trabajar en paralelo sin pisarnos el código, con un sistema simple y seguro.

---

## 📐 Estructura de Ramas

```
main (producción, siempre funcional)
  ↓
dev (integración, donde se unen todas las features)
  ↓
feature/nombre-de-la-tarea (tu código personal)
```

**Reglas sagradas:**
- ❌ **NUNCA** hacer commit directo a `main`
- ❌ **NUNCA** hacer commit directo a `dev`
- ✅ **SIEMPRE** trabajar en tu rama `feature/...`
- ✅ **SIEMPRE** hacer Pull Request para unir tu código

---

## 🚀 Setup Inicial (Solo una vez)

### 1. Instalar Git
- Windows: https://git-scm.com/download/win
- Verificar: abre CMD y escribe `git --version`

### 2. Configurar tu identidad
```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tuemail@ejemplo.com"
```

### 3. Clonar el proyecto
```bash
cd Desktop
git clone <URL-DEL-REPOSITORIO>
cd parking-system
```

### 4. Verificar rama actual
```bash
git branch
# Deberías ver: * dev
```

---

## 🔁 Flujo de Trabajo Diario

### **PASO 1: Antes de empezar a trabajar (SIEMPRE)**
Actualiza tu código con los últimos cambios del equipo:

```bash
# 1. Asegúrate de estar en dev
git checkout dev

# 2. Descarga los últimos cambios
git pull origin dev
```

---

### **PASO 2: Crear tu rama de trabajo**
Cada tarea nueva = nueva rama

```bash
# Formato: feature/descripcion-corta
# Ejemplos:
git checkout -b feature/login-page
git checkout -b feature/api-dashboard
git checkout -b feature/calcular-tarifa
```

**Convención de nombres:**
- `feature/nombre-tarea` → Nueva funcionalidad
- `fix/nombre-bug` → Corrección de error
- `docs/nombre` → Documentación

---

### **PASO 3: Trabajar en tu código**

Haz cambios en tus archivos, luego:

```bash
# Ver qué archivos cambiaste
git status

# Agregar archivos específicos al commit
git add src/pages/LoginPage.jsx
git add src/api/auth.js

# O agregar TODO lo modificado (úsalo con cuidado)
git add .

# Hacer commit con mensaje descriptivo
git commit -m "feat: crear página de login con validación de formulario"
```

**Formato de mensajes de commit:**
```
feat: agregar nueva funcionalidad
fix: corregir bug
style: cambios de estilos/formato
refactor: mejorar código sin cambiar funcionalidad
docs: actualizar documentación
```

---

### **PASO 4: Subir tu rama al repositorio**

```bash
# Primera vez que subes la rama
git push -u origin feature/tu-rama

# Commits siguientes (ya no necesitas -u)
git push
```

---

### **PASO 5: Crear Pull Request (PR)**

1. Ve a GitHub/GitLab
2. Verás un botón "Compare & pull request" → Click
3. Completa la información:
   - **Título:** Descripción breve de tu tarea
   - **Descripción:** 
     ```
     ## ¿Qué hace este PR?
     - Agrega página de login
     - Conecta formulario con API /auth/login
     
     ## ¿Cómo probar?
     1. Ir a /login
     2. Ingresar usuario: admin, password: 123
     3. Verificar que redirija al dashboard
     
     ## Screenshots (si aplica)
     [captura de pantalla]
     ```
4. Asignar reviewer: **Tu líder técnico**
5. Click "Create Pull Request"

---

### **PASO 6: Responder a Code Review**

El líder revisará tu código y puede:
- ✅ **Aprobar:** Tu código pasa → se hace merge a `dev`
- 💬 **Comentar cambios:** Tienes que hacer ajustes

Si te piden cambios:
```bash
# Haz las correcciones en tu rama
# ... editas archivos ...

# Commit de las correcciones
git add .
git commit -m "fix: corregir validación según code review"

# Sube los cambios (el PR se actualiza automáticamente)
git push
```

---

### **PASO 7: Después del Merge**

Tu código ya está en `dev`, ahora limpia:

```bash
# Volver a dev
git checkout dev

# Traer tu código ya mergeado
git pull origin dev

# Borrar tu rama local (ya no la necesitas)
git branch -d feature/tu-rama-vieja
```

---

## 🆘 Comandos de Emergencia

### "Me equivoqué en mi último commit"
```bash
# Deshacer el último commit (pero mantener los cambios)
git reset --soft HEAD~1

# Ahora puedes re-hacer el commit correctamente
git add .
git commit -m "mensaje correcto"
```

### "Quiero descartar TODOS mis cambios locales"
```bash
# ⚠️ CUIDADO: esto borra tus cambios sin guardar
git checkout .
```

### "Descargué cambios y ahora tengo conflictos"
```bash
# Git te marcará los archivos con conflicto
git status  # ver qué archivos tienen conflicto

# Abre cada archivo, busca las marcas:
# <<<<<<< HEAD
# tu código
# =======
# código de otro
# >>>>>>> branch-name

# Decide qué código mantener, borra las marcas (<<<<, ====, >>>>)

# Marca como resuelto
git add archivo-con-conflicto.js

# Termina el merge
git commit -m "fix: resolver conflicto en archivo X"
```

### "Necesito traer cambios de dev a mi rama"
```bash
# Estando en tu rama feature
git checkout feature/mi-rama

# Traer cambios de dev
git merge dev

# Si hay conflictos, resuélvelos como arriba
```

### "Quiero ver el historial de commits"
```bash
git log --oneline --graph --decorate --all
```

### "Olvidé en qué rama estoy"
```bash
git branch
# La rama con * es la actual
```

---

## 🎨 Visualización del Flujo Completo

```
DÍA 1:
  dev (actualizado)
    ↓
  feature/mi-tarea (creas rama)
    ↓
  [trabajo, trabajo, trabajo]
    ↓
  git add + git commit (guardas cambios)
    ↓
  git push (subes al repo)
    ↓
  Pull Request en GitHub

DÍA 2:
  Code Review (líder revisa)
    ↓
  (si hay cambios pedidos)
    ↓
  Corriges en tu rama
    ↓
  git push (actualiza el PR)
    ↓
  Aprobación ✅
    ↓
  Merge a dev
    ↓
  git checkout dev
  git pull
  (ya tienes tu código + el de todos)

SIGUIENTE TAREA:
  git checkout -b feature/nueva-tarea
  [repites el ciclo]
```

---

## 📋 Checklist Antes de Crear un PR

- [ ] Mi código funciona (lo probé localmente)
- [ ] No hay archivos innecesarios (node_modules, .env, archivos de editor)
- [ ] Los mensajes de commit son descriptivos
- [ ] Actualicé mi rama con dev (`git merge dev`)
- [ ] Le agregué descripción clara al PR
- [ ] Asigné un reviewer

---

## 🚫 Errores Comunes y Cómo Evitarlos

### ❌ Error: "No tengo permisos para push a dev"
**Solución:** Estás en la rama equivocada. Cambia a tu feature:
```bash
git checkout feature/tu-rama
git push
```

### ❌ Error: "Mi PR tiene 500 archivos cambiados"
**Causa:** Probablemente subiste `node_modules/` o archivos de build.
**Solución:** Crear/actualizar `.gitignore`:
```
node_modules/
target/
.env
.DS_Store
*.log
```

### ❌ Error: "Dice que tengo conflictos pero no sé qué hacer"
**Solución:** Pide ayuda al líder en pair programming. Es normal al inicio.

### ❌ Error: "Hice commit en dev sin querer"
**Solución:** 
```bash
# Deshacer el commit (mantener cambios)
git reset --soft HEAD~1

# Crear la rama correcta
git checkout -b feature/mi-tarea

# Re-hacer el commit
git add .
git commit -m "tu mensaje"
git push -u origin feature/mi-tarea
```

---

## 🎓 Ejercicio de Práctica (Primer Día)

**Objetivo:** Familiarizarte con el flujo sin miedo.

1. Crea una rama de prueba:
```bash
git checkout dev
git pull
git checkout -b feature/practica-tu-nombre
```

2. Crea un archivo de prueba:
```bash
echo "Hola, soy [TU NOMBRE]" > practica.txt
```

3. Haz commit:
```bash
git add practica.txt
git commit -m "test: archivo de práctica"
```

4. Sube tu rama:
```bash
git push -u origin feature/practica-tu-nombre
```

5. Crea un PR (el líder lo cerrará sin merge, solo es para practicar)

---

## 🤝 Convenciones del Equipo

### ¿Cuándo hacer commit?
- ✅ Cada vez que termines una sub-tarea lógica (ej: "formulario de login funcionando")
- ❌ No cada 5 minutos (commits demasiado pequeños)
- ❌ No al final del día con 10 cambios (commit demasiado grande)

### ¿Cuándo hacer push?
- ✅ Al final del día de trabajo
- ✅ Antes de pedir ayuda sobre tu código
- ✅ Cuando tu funcionalidad esté completa

### ¿Cuándo crear PR?
- ✅ Cuando tu tarea esté 100% terminada y probada
- ❌ No crees PRs "work in progress" sin avisar

---

## 📞 Pedir Ayuda

Si algo no funciona:
1. Lee el mensaje de error completo
2. Copia el error + el comando que ejecutaste
3. Comparte en el grupo:
   ```
   🆘 Ayuda con Git
   Comando: git push
   Error: [pega el error aquí]
   Contexto: estaba subiendo mi rama de login
   ```

---

## 🎯 Resumen Ultra-Rápido

```bash
# === INICIO DEL DÍA ===
git checkout dev
git pull origin dev
git checkout -b feature/mi-tarea

# === TRABAJAR ===
# ... editar archivos ...
git add .
git commit -m "descripción de cambios"

# === SUBIR ===
git push -u origin feature/mi-tarea

# === EN GITHUB ===
Crear Pull Request → Asignar al líder → Esperar review

# === DESPUÉS DEL MERGE ===
git checkout dev
git pull origin dev
```

---

## 🏆 Objetivo Final

Al terminar el proyecto, deberías poder:
- ✅ Crear ramas sin miedo
- ✅ Hacer commits con mensajes claros
- ✅ Resolver conflictos simples
- ✅ Entender el historial de Git
- ✅ Trabajar en paralelo sin bloquear al equipo

**¡Git es tu amigo, no tu enemigo! Con práctica se vuelve natural 🚀**

