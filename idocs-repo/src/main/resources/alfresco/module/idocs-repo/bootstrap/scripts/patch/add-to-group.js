function main() {
    var group = groups.getGroup(args.group);
    for (var i = 0; i < args.members.size(); i++) {
        group.addAuthority(args.members.get(i));
    }
}

main();