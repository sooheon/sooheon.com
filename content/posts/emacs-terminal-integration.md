---
title: Emacs and iTerm Integration
date-published: 2017-02-15
uuid: 568D1B57-555F-45CB-B19D-4374BEDC680E
tags:
 - emacs
 - development
---

The rationale for using graphical Emacs and iTerm together is as follows:

1. Emacs is better as a GUI app.

    Seamless and customizable use of all modifiers (⌘, ⌃-⇧, `Meta` vs. ⌥, etc.),
    distinguishing between the `Return` key and `C-m`, imbedding of LaTeX
    generated svg fragments in notes, variable width and size fonts, and mouse
    support, to say the least. The emacs-mac port adds other niceties such as
    pixel-wise scrolling and window resizing, two-finger swipe, and more.

2. Emacs does not have a good terminal emulator.

    I'm aware that eshell, term, and shell each have their proponents.
    None are satisfactory because each is full of edge cases and annoyances by
    nature of being shoehorned into emacs text buffers. Generally worse
    performance and bugginess is a futher insult. I need a dedicated terminal.

3. Thankfully, many of Emacs's strengths obviate the need for a terminal.

    Magit is a better git interface, CIDER is a REPL on steroids, Ivy and Dired
    are a better Ranger/fzf/file explorer, Counsel is a better interface to
    command line search tools. I don't need a terminal *too* often, so I don't
    mind it being a separate app, as long as the transition back and forth is
    smooth.

I can't promise quite as smooth an integration as between, say, Vim or Kakoune
and tmux, but I've gotten close enough for my needs. So, assuming you're using
both the emacs-mac port (available from homebrew [here][6]) and iTerm, the
integration is as follows:

## Launching Emacs from iTerm

First to launch GUI Emacs from terminal, I am using the following script from
https://gist.github.com/4043945:

```bash
#!/bin/bash

if [ -e '/Applications/Emacs.app' ]; then
  t=()

  if [ ${#@} -ne 0 ]; then
    while IFS= read -r file; do
      [ ! -f "$file" ] && t+=("$file") && /usr/bin/touch "$file"
      file=$(echo $(cd $(dirname "$file") && pwd -P)/$(basename "$file"))
      $(/usr/bin/osascript <<-END
        if application "Emacs.app" is running then
          tell application id (id of application "Emacs.app") to open POSIX file "$file"
        else
          tell application ((path to applications folder as text) & "Emacs.app")
            activate
            open POSIX file "$file"
          end tell
        end if
END
      ) &  # Note: END on the previous line may be indented with tabs but not spaces
    done <<<"$(printf '%s\n' "$@")"
  fi

  if [ ! -z "$t" ]; then
    $(/bin/sleep 10; for file in "${t[@]}"; do
      [ ! -s "$file" ] && /bin/rm "$file";
    done) &
  fi
else
  vim -No "$@"
fi
```

I have it aliased to `e`, so `e filename` will open the file in GUI Emacs. The
benefit of using this script over emacsclient is that this hands over the file
to Emacs immediately and does not wait for editing to complete, meaning you can
continue using the terminal right away.

## Launching iTerm and auto-cd'ing

To open the terminal from Emacs and immediately cd to the current directory, I
took inspiration from justinmk's [vim-gtfo][4] and various other scripts
floating around the internet. It makes use of the elisp function do-applescript.
Importantly, the `if (is at shell prompt of the current session)` checks to see
whether the current terminal is busy, and opens a new split if so.

I can call this at any time, while viewing a file, Dired, or Magit buffer, and
it will activate iTerm and cd to the current directory. I have it bound to
`got`, for "go to terminal", in Normal state (yes, I'm also using [evil][5]). I
also recommend binding `reveal-in-osx-finder`, a package available from MELPA,
to `gof`.

```emacs-lisp
;;;###autoload
(defun soo-terminal-pop ()
  "If iTerm is not open, launch it. If iTerm session is busy, split
off a new pane. Switch to iTerm and cd to default-directory."
  (interactive)
  (do-applescript
   (format "
      tell the current session to write text \"cd %s\"
    end if
  end tell
end tell"
           (or default-directory "~")
           (or default-directory "~"))))
```

Finally, a quick global shortcut ⌘-⌃-t for activating and hiding iTerm,
and I have everything I need for seamless integration.

## The Result

<img src="/images/content/emacs-iterm-workflow.png" alt="My Emacs-iTerm workflow">

The biggest reason that all of this works for me is that this acknowledges that
the ⌘ key exists on my Mac. I was never happy with rebinding it to `Meta` in
Emacs, or generally ignoring its existence inside terminals. With my setup,
there is a very nice symmetry between selecting tabs in Chrome with ⌘+Number,
and selecting a pane/window in iTerm/Emacs with the same. In both Emacs and
iTerm, I move around splits with ⌘-hjkl, and in Emacs I can use all the normal ⌘
based CUA keys. Best of all, since the ⌘ modifier has firm responsibility over
all inter- and intra-app navigation in my mind, the ⌃ and `Meta` keyspaces can
be fully dedicated to editing. For example, [vim-tmux-navigator][3] uses
`⌃-hjkl`, but all four of those keybindings are useful in Emacs; most `Meta`
bindings are similarly important. Using the tmux prefix key every time is not
even worth considering.

[1]: https://bitbucket.org/mituharu/emacs-mac/overview
[2]: https://www.iterm2.com
[3]: https://github.com/christoomey/vim-tmux-navigator 
[4]: https://github.com/justinmk/vim-gtfo
[5]: https://bitbucket.org/lyro/evil/wiki/Home
[6]: https://github.com/railwaycat/homebrew-emacsmacport
