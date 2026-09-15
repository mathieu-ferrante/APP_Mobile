-- ============================================================
-- Phoenix - quota du proxy IA (Edge Function "ai-coach")
-- A executer dans Supabase > SQL Editor > New query
-- ============================================================

-- Compteur d'appels IA par utilisateur et par jour.
create table if not exists public.ai_usage (
  user_id uuid not null references auth.users (id) on delete cascade,
  day     date not null default (now() at time zone 'utc')::date,
  calls   integer not null default 0,
  primary key (user_id, day)
);

alter table public.ai_usage enable row level security;

-- Chaque utilisateur peut consulter sa propre consommation.
-- Aucune policy d'ecriture : seule l'Edge Function ecrit, via service_role.
drop policy if exists "ai_usage_select_own" on public.ai_usage;
create policy "ai_usage_select_own"
  on public.ai_usage for select
  using (auth.uid() = user_id);

-- Incremente le compteur du jour et indique si l'appel reste dans le quota.
-- SECURITY DEFINER : la fonction ecrit malgre l'absence de policy d'insertion.
create or replace function public.ai_consume_quota(p_user uuid, p_limit integer)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
  v_day   date := (now() at time zone 'utc')::date;
  v_calls integer;
begin
  insert into public.ai_usage (user_id, day, calls)
  values (p_user, v_day, 1)
  on conflict (user_id, day)
    do update set calls = ai_usage.calls + 1
  returning calls into v_calls;

  return v_calls <= p_limit;
end;
$$;

-- Personne d'autre que l'Edge Function ne doit pouvoir appeler cette fonction.
revoke all on function public.ai_consume_quota(uuid, integer) from public;
revoke all on function public.ai_consume_quota(uuid, integer) from anon;
revoke all on function public.ai_consume_quota(uuid, integer) from authenticated;
grant execute on function public.ai_consume_quota(uuid, integer) to service_role;

-- Purge des compteurs de plus de 90 jours (optionnel, a appeler ponctuellement).
create or replace function public.ai_usage_cleanup()
returns void
language sql
security definer
set search_path = public
as $$
  delete from public.ai_usage
  where day < (now() at time zone 'utc')::date - 90;
$$;
