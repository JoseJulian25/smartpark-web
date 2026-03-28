import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent } from '../components/ui/card';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Badge } from '../components/ui/badge';
import { Alert, AlertDescription } from '../components/ui/alert';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../components/ui/select';
import { UserPlus, Edit, Trash2, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import {
  getUsuarios,
  addUsuario,
  updateUsuario,
  updateEstadoUsuario,
  deleteUsuario,
  getErrorMessage,
} from '../api/usuarios';

export const UsuariosPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [showDialog, setShowDialog] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [userToDelete, setUserToDelete] = useState(null);

  const [formData, setFormData] = useState({
    username: '',
    password: '',
    nombre: '',
    rol: 'operador',
  });

  const [searchTerm, setSearchTerm] = useState('');
  const [rolFilter, setRolFilter] = useState('all');
  const [estadoFilter, setEstadoFilter] = useState('all');
  const [sortConfig, setSortConfig] = useState({ key: 'createdAt', direction: 'desc' });

  const isAdmin = (rol) => (rol || '').toLowerCase() === 'admin';

  useEffect(() => {
    fetchUsuarios();
  }, []);

  const fetchUsuarios = async (silent = false) => {
    try {
      if (!silent) {
        setLoading(true);
      }
      const data = await getUsuarios();
      setUsers(data);
    } catch (err) {
      toast.error(getErrorMessage(err, 'Error cargando usuarios'));
    } finally {
      if (!silent) {
        setLoading(false);
      }
    }
  };

  // ── Abrir/cerrar dialog ──────────────────────────────────────────
  const handleOpenDialog = (user) => {
    if (user) {
      setEditingUser(user);
      setFormData({
        username: user.username,
        password: '',
        nombre: user.nombre,
        rol: user.rol?.toLowerCase() || 'operador',
      });
    } else {
      setEditingUser(null);
      setFormData({ username: '', password: '', nombre: '', rol: 'operador' });
    }
    setShowDialog(true);
  };

  const handleCloseDialog = () => {
    setShowDialog(false);
    setEditingUser(null);
    setFormData({ username: '', password: '', nombre: '', rol: 'operador' });
  };

  // ── Guardar usuario ──────────────────────────────────────────────
  const handleSubmit = async (e) => {
    e.preventDefault();

    const username = formData.username.trim();
    const nombre = formData.nombre.trim();
    const password = formData.password.trim();

    if (!username || !nombre) {
      toast.error('Usuario y nombre son requeridos');
      return;
    }

    if (!editingUser && !password) {
      toast.error('La contraseña es requerida para nuevos usuarios');
      return;
    }

    if (password && password.length < 6) {
      toast.error('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    try {
      setSaving(true);

      const payload = {
        username,
        nombre,
        rol: formData.rol,
      };

      if (editingUser) {
        if (password) {
          payload.password = password;
        }
        await updateUsuario(editingUser.id, payload);
        toast.success('Usuario actualizado exitosamente');
      } else {
        await addUsuario({
          ...payload,
          password,
        });
        toast.success('Usuario creado exitosamente');
      }

      await fetchUsuarios(true);
      handleCloseDialog();
    } catch (err) {
      toast.error(getErrorMessage(err, 'No se pudo guardar el usuario'));
    } finally {
      setSaving(false);
    }
  };

  // ── Eliminar usuario ─────────────────────────────────────────────
  const handleDeleteClick = (user) => {
    setUserToDelete(user);
    setShowDeleteDialog(true);
  };

  const handleDeleteConfirm = async () => {
    if (!userToDelete) return;

    const activeAdmins = users.filter((u) => isAdmin(u.rol) && u.activo);
    if (isAdmin(userToDelete.rol) && activeAdmins.length === 1) {
      toast.error('No se puede eliminar el único administrador activo');
      setShowDeleteDialog(false);
      return;
    }

    try {
      setSaving(true);
      await deleteUsuario(userToDelete.id);
      await fetchUsuarios(true);
      toast.success('Usuario eliminado exitosamente');
      setShowDeleteDialog(false);
      setUserToDelete(null);
    } catch (err) {
      toast.error(getErrorMessage(err, 'No se pudo eliminar el usuario'));
    } finally {
      setSaving(false);
    }
  };

  // ── Activar / Desactivar ─────────────────────────────────────────
  const handleToggleStatus = async (user) => {
    if (isAdmin(user.rol) && user.activo) {
      const activeAdmins = users.filter((u) => isAdmin(u.rol) && u.activo);
      if (activeAdmins.length === 1) {
        toast.error('No se puede desactivar el único administrador activo');
        return;
      }
    }

    try {
      setSaving(true);
      await updateEstadoUsuario(user.id, !user.activo);
      await fetchUsuarios(true);
      toast.success(`Usuario ${!user.activo ? 'activado' : 'desactivado'} exitosamente`);
    } catch (err) {
      toast.error(getErrorMessage(err, 'No se pudo actualizar el estado del usuario'));
    } finally {
      setSaving(false);
    }
  };

  const formatDate = (value) => {
    if (!value) {
      return '-';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return '-';
    }
    return date.toLocaleDateString('es-DO');
  };

  const getSortableValue = (user, key) => {
    switch (key) {
      case 'username':
        return (user.username || '').toLowerCase();
      case 'nombre':
        return (user.nombre || '').toLowerCase();
      case 'rol':
        return (user.rol || '').toLowerCase();
      case 'activo':
        return user.activo ? 1 : 0;
      case 'createdAt':
        return new Date(user.createdAt || 0).getTime() || 0;
      default:
        return '';
    }
  };

  const handleSort = (key) => {
    setSortConfig((prev) => {
      if (prev.key === key) {
        return {
          key,
          direction: prev.direction === 'asc' ? 'desc' : 'asc',
        };
      }

      return {
        key,
        direction: 'asc',
      };
    });
  };

  const getSortIndicator = (key) => {
    if (sortConfig.key !== key) {
      return '↕';
    }

    return sortConfig.direction === 'asc' ? '↑' : '↓';
  };

  const filteredAndSortedUsers = useMemo(() => {
    let result = [...users];

    const term = searchTerm.trim().toLowerCase();
    if (term) {
      result = result.filter(
        (user) =>
          (user.username || '').toLowerCase().includes(term) ||
          (user.nombre || '').toLowerCase().includes(term)
      );
    }

    if (rolFilter !== 'all') {
      result = result.filter((user) => (user.rol || '').toLowerCase() === rolFilter);
    }

    if (estadoFilter !== 'all') {
      const expectedActivo = estadoFilter === 'activo';
      result = result.filter((user) => user.activo === expectedActivo);
    }

    result.sort((a, b) => {
      const direction = sortConfig.direction === 'asc' ? 1 : -1;
      const aValue = getSortableValue(a, sortConfig.key);
      const bValue = getSortableValue(b, sortConfig.key);

      if (typeof aValue === 'number' && typeof bValue === 'number') {
        return (aValue - bValue) * direction;
      }

      return String(aValue).localeCompare(String(bValue), 'es', { sensitivity: 'base' }) * direction;
    });

    return result;
  }, [users, searchTerm, rolFilter, estadoFilter, sortConfig]);

  // ── Render ───────────────────────────────────────────────────────
  return (
    <div className="space-y-4">
      {/* Encabezado */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Gestión de Usuarios</h1>
        </div>
        <Button onClick={() => handleOpenDialog()}>
          <UserPlus className="w-4 h-4 mr-2" />
          Nuevo Usuario
        </Button>
      </div>

      {loading && (
        <Alert>
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>Cargando usuarios...</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardContent className="p-4 grid grid-cols-1 gap-3 md:grid-cols-3">
          <div className="space-y-2">
            <Label htmlFor="search">Buscar</Label>
            <Input
              id="search"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Usuario o nombre"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="rol-filter">Rol</Label>
            <Select value={rolFilter} onValueChange={setRolFilter}>
              <SelectTrigger id="rol-filter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos</SelectItem>
                <SelectItem value="admin">Administrador</SelectItem>
                <SelectItem value="operador">Operador</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="estado-filter">Estado</Label>
            <Select value={estadoFilter} onValueChange={setEstadoFilter}>
              <SelectTrigger id="estado-filter">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todos</SelectItem>
                <SelectItem value="activo">Activos</SelectItem>
                <SelectItem value="inactivo">Inactivos</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {!loading && (
        <Card>
          <CardContent className="p-4">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>
                    <button
                      type="button"
                      className="inline-flex items-center gap-1 hover:text-foreground"
                      onClick={() => handleSort('username')}
                    >
                      Usuario <span className="text-xs">{getSortIndicator('username')}</span>
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      type="button"
                      className="inline-flex items-center gap-1 hover:text-foreground"
                      onClick={() => handleSort('nombre')}
                    >
                      Nombre <span className="text-xs">{getSortIndicator('nombre')}</span>
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      type="button"
                      className="inline-flex items-center gap-1 hover:text-foreground"
                      onClick={() => handleSort('rol')}
                    >
                      Rol <span className="text-xs">{getSortIndicator('rol')}</span>
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      type="button"
                      className="inline-flex items-center gap-1 hover:text-foreground"
                      onClick={() => handleSort('activo')}
                    >
                      Estado <span className="text-xs">{getSortIndicator('activo')}</span>
                    </button>
                  </TableHead>
                  <TableHead>
                    <button
                      type="button"
                      className="inline-flex items-center gap-1 hover:text-foreground"
                      onClick={() => handleSort('createdAt')}
                    >
                      Fecha de Creación <span className="text-xs">{getSortIndicator('createdAt')}</span>
                    </button>
                  </TableHead>
                  <TableHead>Acciones</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredAndSortedUsers.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center text-muted-foreground py-8">
                      No hay usuarios que coincidan con los filtros
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredAndSortedUsers.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell className={`font-medium ${!user.activo ? 'text-muted-foreground' : ''}`}>
                        {user.username}
                      </TableCell>
                      <TableCell className={!user.activo ? 'text-muted-foreground' : ''}>{user.nombre}</TableCell>
                      <TableCell>
                        <Badge
                          variant={isAdmin(user.rol) ? 'default' : 'secondary'}
                          className={`capitalize ${!user.activo ? 'opacity-70' : ''}`}
                        >
                          {(user.rol || '').toLowerCase()}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant="outline"
                          className={
                            user.activo
                              ? 'bg-green-50 text-green-700 border-green-200'
                              : 'bg-gray-50 text-gray-700 border-gray-200'
                          }
                        >
                          {user.activo ? 'Activo' : 'Inactivo'}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {formatDate(user.createdAt)}
                      </TableCell>
                      <TableCell>
                        <div className="flex gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={saving}
                            onClick={() => handleOpenDialog(user)}
                          >
                            <Edit className="w-3 h-3" />
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={saving}
                            onClick={() => handleToggleStatus(user)}
                          >
                            {user.activo ? 'Desactivar' : 'Activar'}
                          </Button>
                          <Button
                            size="sm"
                            variant="destructive"
                            disabled={saving}
                            onClick={() => handleDeleteClick(user)}
                          >
                            <Trash2 className="w-3 h-3" />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* ── Dialog: Crear / Editar ── */}
      <Dialog open={showDialog} onOpenChange={handleCloseDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingUser ? 'Editar Usuario' : 'Nuevo Usuario'}</DialogTitle>
            <DialogDescription>
              {editingUser
                ? 'Modifique la información del usuario'
                : 'Complete los datos del nuevo usuario'}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit}>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label htmlFor="username">Usuario *</Label>
                <Input
                  id="username"
                  value={formData.username}
                  onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                  placeholder="nombre.usuario"
                  autoComplete="off"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="nombre">Nombre Completo *</Label>
                <Input
                  id="nombre"
                  value={formData.nombre}
                  onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                  placeholder="Juan Pérez"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">
                  Contraseña {editingUser ? '(dejar vacío para no cambiar)' : '*'}
                </Label>
                <Input
                  id="password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  placeholder={editingUser ? 'Nueva contraseña' : 'Contraseña'}
                  autoComplete="new-password"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="rol">Rol *</Label>
                <Select
                  value={formData.rol}
                  onValueChange={(value) => setFormData({ ...formData, rol: value })}
                >
                  <SelectTrigger id="rol">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="operador">Operador</SelectItem>
                    <SelectItem value="admin">Administrador</SelectItem>
                  </SelectContent>
                </Select>
              </div>

            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={handleCloseDialog}>
                Cancelar
              </Button>
              <Button type="submit" disabled={saving}>
                {editingUser ? 'Guardar Cambios' : 'Crear Usuario'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* ── Dialog: Confirmar eliminación ── */}
      <Dialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Eliminar Usuario</DialogTitle>
            <DialogDescription>
              ¿Está seguro que desea eliminar este usuario? Esta acción no se puede deshacer.
            </DialogDescription>
          </DialogHeader>

          {userToDelete && (
            <div className="py-4">
              <p className="text-sm">
                <strong>Usuario:</strong> {userToDelete.username}
              </p>
              <p className="text-sm">
                <strong>Nombre:</strong> {userToDelete.nombre}
              </p>
            </div>
          )}

          <DialogFooter>
            <Button variant="outline" onClick={() => setShowDeleteDialog(false)}>
              Cancelar
            </Button>
            <Button variant="destructive" disabled={saving} onClick={handleDeleteConfirm}>
              Eliminar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};
