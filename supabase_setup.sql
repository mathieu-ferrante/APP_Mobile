-- ============================================================
-- Phoenix - schema de synchronisation multi-appareils
-- A executer une seule fois dans Supabase > SQL Editor > New query
-- ============================================================

-- Table unique : un snapshot JSON complet par utilisateur.
create table if not exists public.user_data (
  user_id    uuid primary key references auth.users (id) on delete cascade,
  payload    jsonb       not null,
  updated_at timestamptz not null default now()
);

-- Chaque utilisateur ne voit et ne modifie QUE sa propre ligne.
alter table public.user_data enable row level security;

drop policy if exists "user_data_select_own" on public.user_data;
create policy "user_data_select_own"
  on public.user_data for select
  using (auth.uid() = user_id);

drop policy if exists "user_data_insert_own" on public.user_data;
create policy "user_data_insert_own"
  on public.user_data for insert
  with check (auth.uid() = user_id);

drop policy if exists "user_data_update_own" on public.user_data;
create policy "user_data_update_own"
  on public.user_data for update
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

drop policy if exists "user_data_delete_own" on public.user_data;
create policy "user_data_delete_own"
  on public.user_data for delete
  using (auth.uid() = user_id);

-- Horodatage automatique a chaque ecriture.
create or replace function public.touch_user_data()
returns trigger
language plpgsql
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists user_data_touch on public.user_data;
create trigger user_data_touch
  before update on public.user_data
  for each row execute function public.touch_user_data();
