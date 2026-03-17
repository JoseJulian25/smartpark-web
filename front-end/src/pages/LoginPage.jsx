import { useAuth } from "../contexts/AuthContext";

export  function LoginPage() {
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const username = e.target.username.value;
    const password = e.target.password.value;

    const success = await login(username, password);
    if (success) {
      window.location.href = "/dashboard";
    } else {
      alert("Usuario o contraseña incorrectos");
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="username" placeholder="Usuario" />
      <input type="password" name="password" placeholder="Contraseña" />
      <button type="submit">Ingresar</button>
    </form>
  );
}