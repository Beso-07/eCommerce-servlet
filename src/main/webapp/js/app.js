function validateLoginForm() {
  return true;
}

function validateRegisterForm() {
  const form = document.getElementById("registerForm");
  const errorBox = document.getElementById("registerClientError");
  const name = form?.querySelector('input[name="name"]');
  const email = form?.querySelector('input[name="email"]');
  const password = document.querySelector('input[name="password"]');
  const confirm = document.querySelector('input[name="confirmPassword"]');
  if (!form || !name || !email || !password || !confirm) return true;

  if (errorBox) {
    errorBox.classList.add("d-none");
    errorBox.textContent = "";
  }

  if (!name.value.trim() || !email.value.trim() || !password.value.trim() || !confirm.value.trim()) {
    if (errorBox) {
      errorBox.textContent = "All fields are required.";
      errorBox.classList.remove("d-none");
    }
    return false;
  }

  if (password.value.length < 8) {
    if (errorBox) {
      errorBox.textContent = "Password must be at least 8 characters.";
      errorBox.classList.remove("d-none");
    }
    return false;
  }

  if (password.value !== confirm.value) {
    if (errorBox) {
      errorBox.textContent = "Passwords do not match.";
      errorBox.classList.remove("d-none");
    }
    return false;
  }
  return true;
}
