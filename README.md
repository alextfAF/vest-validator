# create-capacitor-plugin

validate if a person in a selfie has a vest

## Install

```bash
npm install create-capacitor-plugin
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`checkHasVest(...)`](#checkhasvest)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### checkHasVest(...)

```typescript
checkHasVest(options: HasVestOptions) => Promise<{ hasVest: boolean; }>
```

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#hasvestoptions">HasVestOptions</a></code> |

**Returns:** <code>Promise&lt;{ hasVest: boolean; }&gt;</code>

--------------------


### Interfaces


#### HasVestOptions

| Prop           | Type                 |
| -------------- | -------------------- |
| **`image`**    | <code>string</code>  |
| **`showLogs`** | <code>boolean</code> |

</docgen-api>
# vest-validator
