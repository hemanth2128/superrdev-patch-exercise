const API_BASE = '/api';

// Only these values are accepted by the backend (TaskStatus enum).
// Anything else (e.g. "all", "undefined" from a stale bundle) is dropped
// so it can never produce a 400.
const VALID_STATUSES = ['OPEN', 'IN_PROGRESS', 'DONE'];

export async function fetchTasks({ query = '', status = '', page = 1, pageSize = 10 }, signal) {
  // Coerce page/pageSize to safe integers: non-numeric input (NaN,
  // undefined, "abc") would otherwise make Spring return 400 on
  // @RequestParam int binding.
  const safePage = Number.parseInt(page, 10);
  const safePageSize = Number.parseInt(pageSize, 10);

  const params = new URLSearchParams();
  if (query) params.set('q', query);
  if (VALID_STATUSES.includes(status)) params.set('status', status);
  params.set('page', String(Number.isFinite(safePage) && safePage >= 1 ? safePage : 1));
  params.set('pageSize', String(
    Number.isFinite(safePageSize) ? Math.min(Math.max(safePageSize, 1), 50) : 10
  ));

  const url = `${API_BASE}/tasks?${params.toString()}`;

  // This API uses no cookies or auth. Omit ambient credentials so cookies
  // shared with other localhost apps are never sent (a malformed/oversized
  // localhost cookie can make the server reject the request with 400).
  const response = await fetch(url, { signal, credentials: 'omit' });

  if (!response.ok) {
    // Surface the backend's error body (e.g. {"error":"Invalid status: all"})
    // instead of a bare status code so the UI message is actionable.
    let detail = '';
    try {
      const body = await response.clone().json();
      if (body && typeof body.error === 'string' && body.error) {
        detail = ` — ${body.error}`;
      }
    } catch {
      // Non-JSON error body: fall back to status code only.
    }
    throw new Error(`Request failed: ${response.status}${detail}`);
  }

  return response.json();
}
