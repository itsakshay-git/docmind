# Styles

DocMind uses plain CSS split by product area. Keep selector names stable unless the related component markup is updated and manually verified.

Import order is controlled by `index.css`:

1. `01-tokens-base.css`: theme variables, reset, primitives, shared UI.
2. `02-auth.css`: login/register landing screen.
3. `03-library-shell.css`: app shell, notebook library, notebook cards, settings shell.
4. `04-workspace-chat.css`: notebook workspace, sources, chat.
5. `05-studio.css`: Studio cards, artifact apps, flashcards, quiz, podcast, infographic.
6. `06-settings.css`: settings detail panels and forms.
7. `07-responsive.css`: mobile/tablet overrides.
