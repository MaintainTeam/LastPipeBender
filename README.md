<p align="center"><a href="https://newpipe.net"><img src="https://img.shields.io/badge/This_is_a_placeholder_for_logo-red" width="150"></a></p> 
<h2 align="center"><b>LastPipeBender</b></h2>
<h4 align="center">A libre lightweight streaming front-end for Android - Fork of NewPipe & Tubular</h4>

<p align="center"><a href="https://maintainteam.github.io/fdroid-pages/fdroid/repo/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on-en.svg" alt="Get it on F-Droid" height=80/></a><br><sup>(F-Droid repo hosted by us, import it to "Repositories")</sup></p><p align="center">


<p align="center">
<a href="https://github.com/MaintainTeam/LastPipeBender"><img src="https://img.shields.io/github/v/release/MaintainTeam/LastPipeBender?style=flat-square&color=orange" alt="GitHub release"></a>
<a href="https://www.gnu.org/licenses/gpl-3.0"><img src="https://img.shields.io/badge/license-GPL%20v3-blue?style=flat-square" alt="License: GPLv3"></a>
<a href="https://github.com/MaintainTeam/LastPipeBender/actions"><img src="https://img.shields.io/github/actions/workflow/status/MaintainTeam/LastPipeBender/ci.yml?style=flat-square" alt="Build Status"></a>
<!-- <a href="https://hosted.weblate.org/engage/newpipe/"><img src="https://img.shields.io/weblate/progress/newpipe?style=flat-square" alt="Upstream Translation Status"></a> -->
<!--<a href="https://web.libera.chat/#newpipe"><img src="https://img.shields.io/badge/IRC%20chat-%23newpipe-brightgreen.svg?style=flat-square" alt="IRC channel: #newpipe"></a>-->
<a href="https://matrix.to/#/!tYUpeILeZnyZspckwY:matrix.org?via=matrix.org"><img src="https://img.shields.io/badge/Matrix%20chat-%23pipebender-blue?style=flat-square" alt="Matrix channel: #newpipe"></a>
</p>

### General Information
This project has been started for merging Tubular features with latest NewPipe fixes. Later we decided to improve it and created [extended version](https://github.com/MaintainTeam/LastPipeBender/wiki/Extended-Version) to implement new features. While both versions will be the soft-fork (backward-compatible) of NewPipe project, extended version has [new features](https://github.com/MaintainTeam/LastPipeBender/wiki/Extended-Version#list-of-extended-features) and may include some minor bugs. 

We created a Roadmap with mostly feature requests. These features will be added to project after passive maintenance mode ends. See the related [discussion](https://github.com/maintainteam/lastpipebender/discussions/6) for more information about **Roadmap & Project Status** 

### Upstream Projects
- [NewPipe](https://github.com/TeamNewPipe/NewPipe)
  - This is the core of our Project ! 
  - Thanks to TeamNewPipe Maintainers and [Contributors](https://github.com/TeamNewPipe/NewPipe/graphs/contributors)
  - [Donate](https://newpipe.net/donate/)
- [Tubular](https://github.com/polymorphicshade/Tubular)
  - This is the fork that we merged <abbr title="ReturnYouTubeDislike">RYD</abbr> and SponsorBlock features
  - Thanks to @polymorphicshade
- [Other forks/apps to merge other cool features from](https://github.com/MaintainTeam/LastPipeBender/wiki/Alternative-YouTube-Clients-List)


### APK Info & Security

Both Debug and Release versions built by GitHub Actions. You can check checksum notice in Release Actions or/and checksum.txt in releases to compare with Application's

This is the SHA fingerprint of LastPipeBender's signing key to verify downloaded APKs which are signed by us.
```
1B:00:8D:64:BB:95:AB:47:74:D6:8B:87:F2:2B:8B:E9:A2:72:F4:92:4D:F5:20:29:D7:E6:18:38:35:D9:18:CC
```

### Project Management

```mermaid
---
title: Project Management
---
graph TD
    B([Tubular]) --> D
    A([NewPipe]) --> D[dev]
    C([features from other forks/apps]) --> | review | F
    D --> | test & mini changes | E[master]
    D --> | unique features | F[extended-dev]

    G .-> | ensure long-term compatibility | E
    E --> K>Stable Version]

    F --> | review | G[extended]
    G --> H>Extended Version]

    F --> P[extended-refactor]
    A --> | refactor branch changes | P
    P --> Y>Refactor Build - WIP]
```

## License
[![GNU GPLv3](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)
