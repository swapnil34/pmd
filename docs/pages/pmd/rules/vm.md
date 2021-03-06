---
title: VM Rules
tags: [rule_references, vm]
summary: Index of all built-in rules available for VM
language_name: VM
permalink: pmd_rules_vm.html
folder: pmd/rules
---
## Best Practices

{% include callout.html content="Rules which enforce generally accepted best practices." %}

*   [AvoidReassigningParameters](pmd_rules_vm_bestpractices.html#avoidreassigningparameters): Reassigning values to incoming parameters is not recommended.  Use temporary local variables inst...
*   [UnusedMacroParameter](pmd_rules_vm_bestpractices.html#unusedmacroparameter): Avoid unused macro parameters. They should be deleted.

## Design

{% include callout.html content="Rules that help you discover design issues." %}

*   [AvoidDeeplyNestedIfStmts](pmd_rules_vm_design.html#avoiddeeplynestedifstmts): Avoid creating deeply nested if-then statements since they are harder to read and error-prone to ...
*   [CollapsibleIfStatements](pmd_rules_vm_design.html#collapsibleifstatements): Sometimes two consecutive 'if' statements can be consolidated by separating their conditions with...
*   [ExcessiveTemplateLength](pmd_rules_vm_design.html#excessivetemplatelength): The template is too long. It should be broken up into smaller pieces.
*   [NoInlineJavaScript](pmd_rules_vm_design.html#noinlinejavascript): Avoid inline JavaScript. Import .js files instead.
*   [NoInlineStyles](pmd_rules_vm_design.html#noinlinestyles): Avoid inline styles. Use css classes instead.

## Error Prone

{% include callout.html content="Rules to detect constructs that are either broken, extremely confusing or prone to runtime errors." %}

*   [EmptyForeachStmt](pmd_rules_vm_errorprone.html#emptyforeachstmt): Empty foreach statements should be deleted.
*   [EmptyIfStmt](pmd_rules_vm_errorprone.html#emptyifstmt): Empty if statements should be deleted.

## Additional rulesets

*   Basic Velocity (`rulesets/vm/basic.xml`):

    <span style="border-radius: 0.25em; color: #fff; padding: 0.2em 0.6em 0.3em; display: inline; background-color: #d9534f; font-size: 75%;">Deprecated</span>  This ruleset is for backwards compatibility.

    It contains the following rules:

    [AvoidDeeplyNestedIfStmts](pmd_rules_vm_design.html#avoiddeeplynestedifstmts), [AvoidReassigningParameters](pmd_rules_vm_bestpractices.html#avoidreassigningparameters), [CollapsibleIfStatements](pmd_rules_vm_design.html#collapsibleifstatements), [EmptyForeachStmt](pmd_rules_vm_errorprone.html#emptyforeachstmt), [EmptyIfStmt](pmd_rules_vm_errorprone.html#emptyifstmt), [ExcessiveTemplateLength](pmd_rules_vm_design.html#excessivetemplatelength), [NoInlineJavaScript](pmd_rules_vm_design.html#noinlinejavascript), [NoInlineStyles](pmd_rules_vm_design.html#noinlinestyles), [UnusedMacroParameter](pmd_rules_vm_bestpractices.html#unusedmacroparameter)


