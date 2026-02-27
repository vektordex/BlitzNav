README.md – BlitzNav

BlitzNav

BlitzNav is a navigation and movement helper library for Blitz3D projects. It provides simple node-based navigation, entity alignment utilities, and movement helpers designed for games and simulations.

License: Creative Commons Attribution 4.0 (CC BY 4.0)
Author: Dex Dirks

Features

- Node-based navigation system

- Entity alignment helpers

- Smooth rotation toward targets

- Movement interpolation

- Optional debug visualization support

- Designed for low-overhead use in real-time scenes

Core Concept

BlitzNav separates:

- Navigation logic (where to go)

- Movement logic (how to move)

- Rotation alignment (how to face targets)

This keeps AI logic clean and reusable across projects.

Example Usage

Include "BlitzNav.bb"

; Example pseudo usage
AlignEntity(PlayerMesh, TargetEntity)
MoveEntity PlayerMesh, 0, 0, 0.25

Intended Use Cases

- AI-controlled characters

- NPC systems

License

BlitzNav is licensed under Creative Commons Attribution 4.0 International (CC BY 4.0).
See LICENSE.md for full details.
