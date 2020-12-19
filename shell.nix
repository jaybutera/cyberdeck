{ pkgs ? import <nixpkgs> {} }:
pkgs.mkShell {
  buildInputs = [
    pkgs.maven
    pkgs.adoptopenjdk-bin
    pkgs.clojure
    pkgs.leiningen
  ];
  #shellHook = "lein figwheel";
}
