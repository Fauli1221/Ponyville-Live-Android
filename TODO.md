Things yet to be done in this application
=========================================

ADD USER ACTIONABLE ERROR MESSAGES FOR API HANDLING!

You foal tinker -> placeholder not working in adapters because holder target is used!
  Set placeholder manually on holder create/recycle, wait to target to finish

To add to action menu:
  Sort (contextually)
    Save selection to user settings, restore on return
  Bulk add to playlist

Better splash? As separate activity?
Incorporate Kez images!
Fix player layout
Try getMediaInformation call on playlist_item creation?

Custom marquee scroll textview?
  UI handler call to cycle char positions
    Check CPU usage
  Change textview width from wrap_content to match_parent; see if this helps


Notifications! Only start service on media start?
Disable Cast until service started? Or just check for bound status and await connection?

Remove icon from glance on player layout
  Just “NOW PLAYING” and ImageButtons
    Heart, PP, Resize

So… simply don’t use service .initNotification()? And set OnStartCommand to return sticky?

ADD TUTORIAL ON FIRST RUN!

MPS
  Proper forward and back playlist support
  Get notifications building again: title, artist e.g.
  Add action to close on notification for newer SDK builds?
    I.e. Check for latest that supports .showCancel, if higher than add 2 actions
One blank, one to close (to separate close action; prevents accidental)

Prettify now playing segment, add disappear controls, update metadata
Need to get station detail, and show/convention list and detail views!

Check that stream pause doesn’t use network data
  IT DOES
  Add alert that informs user of this after a while
Check that internet change continues playback
Listen for internet change -> show message on non-wifi

fullscreen option for video
wake locks on fullscreen video
handle notification content intents (load station detail)
video stations??? load all, check for enabled/active??

Error handling on http protocol errors
Handling for change in network
Tutorial activity
Notifications!
Initial and null playlist notifications
Initial hidden playlist slider
Anchor, main content not resized
Controls for anchor vs full?
Disable (make transparent) main content overlay on sliding
Double local overlay providers...?
Graphic for nav drawer
Update playlist item template, include image e.g.
Update playlist handling, not a queue!
Show and convention listings!
Detail views for all types!


Longterm:
Push handling
Activities for settings and about
Pony.fm integration

Extra content urls to add support for (ie. process for actual streaming url):
  "web_url": "https://www.mixcloud.com/SonicDash/dashessions-episode-61/"
  "web_url": "http://bronyquestpodcast.libsyn.com/brony-quest-45-dragon-quest"
  "web_url": "http://bronytime.libsyn.com/episode-95-the-legendary-people-behind-the-legend-of-equestria"
  "web_url": "http://www.canterlotradio.com/?p=989"
  "web_url": "http://replay.radiobrony.fr/hebdopony/110"
  "web_url": "http://www.buzzsprout.com/16090/301049-lunar-echoes-ep-71-crossovers.mp3"
  "web_url": "http://thebronyshow.net/the-brony-show-mini-show-3/"
  "web_url": "https://fillydelphiaradio.net/podcasts/the-fillycast/?name=2014-11-04_the_fillycast_season_4_episode_6_puppy_love_(ft_derpyknight_and_sujamma).m4a"
  "web_url": "http://ravegn.com/magicshow/?p=episode&name=2013-11-06_finalep5-128.mp3"
  "web_url": "http://www.blogtalkradio.com/ponytoast/2015/10/11/ter-152-five-years-of-autism--happy-birthday-mlp-and-toast"
  "web_url": "http://feedproxy.google.com/~r/VoiceOfEquestriaPodcast/~3/KRjOTX_5rf0/"

  "stream_url": "http://www.bronytv.net/stream.html",
  "stream_url": "https://streamup.com/PonyvilleLive",
  "stream_url": "http://www.livestream.com/alicornradio",

PSEUDO CODE FOR ABOVE
  LiveStream
    Get playlist m3u8 file from:
     <input type="hidden" id="network_name" value=""/>
        <input type="hidden" id="category" value="Entertainment"/>
        <span id="iPhoneUrl" style="display:none;">http://xthebronyshowx.api.channel.livestream.com/3.0/playlist.m3u8</span>
        <span id="isLive" style="display:none;">true</span>
        <span id="isClipMobileCompatible" style="display:none;"></span>
  StreamUp
    Not public
    Within proper page:
      $.ajax({
              url: "https://lancer.streamup.com/api/channels/bronyville-podcast/playlists",
              type: 'GET'
              })
              .done(function(response) {
                  playStream(response['mpd'], response['hls']);
              })
              .fail(function() {
                  playStream('https://streamup.com/404/manifest.mpd', 'https://streamup.com/404/playlist.m3u8');
              });


    Send GET to that url, parse response
      Try stream on ‘hls’

    Either hardcode api urls or follow through to iframe code on embedded (ie bronytv)
      Hardcode :)

Need to run getMediaInformation() on return stream url regardless, check is valid
  If not, use original web_url and create an Intent to open url in Web
    Or possibly incorporate our own WebView in app?
